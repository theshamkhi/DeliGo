package com.shamkhi.deligo.domain.security.oauth2;

import com.shamkhi.deligo.domain.security.model.AuthProvider;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.RoleRepository;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest oAuth2UserRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(oAuth2UserRequest);

        try {
            return processOAuth2User(oAuth2UserRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Erreur lors du traitement de l'utilisateur OAuth2", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex.getCause());
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest oAuth2UserRequest, OAuth2User oAuth2User) {
        String registrationId = oAuth2UserRequest.getClientRegistration().getRegistrationId();

        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(
                registrationId,
                oAuth2User.getAttributes()
        );

        if (!StringUtils.hasText(oAuth2UserInfo.getEmail())) {
            throw new OAuth2AuthenticationException("Email non trouvé depuis le provider OAuth2");
        }

        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            validateProvider(user, registrationId);
            user = updateExistingUser(user, oAuth2UserInfo);
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo);
        }

        // Retourner l'OAuth2User avec l'ID utilisateur comme attribut
        return new DefaultOAuth2User(
                oAuth2User.getAuthorities(),
                oAuth2User.getAttributes(),
                "sub"
        );
    }

    private void validateProvider(User user, String registrationId) {
        if (!user.getProvider().toString().equalsIgnoreCase(registrationId)) {
            throw new OAuth2AuthenticationException(
                    "Vous êtes déjà inscrit avec " + user.getProvider() +
                            ". Veuillez utiliser votre compte " + user.getProvider() + " pour vous connecter."
            );
        }
    }

    private User registerNewUser(OAuth2UserRequest oAuth2UserRequest, OAuth2UserInfo oAuth2UserInfo) {
        log.info("Création d'un nouvel utilisateur OAuth2: {}", oAuth2UserInfo.getEmail());

        User user = User.builder()
                .username(generateUsername(oAuth2UserInfo.getEmail()))
                .email(oAuth2UserInfo.getEmail())
                .nom(oAuth2UserInfo.getLastName() != null ? oAuth2UserInfo.getLastName() : "")
                .prenom(oAuth2UserInfo.getFirstName() != null ? oAuth2UserInfo.getFirstName() : oAuth2UserInfo.getName())
                .provider(AuthProvider.valueOf(oAuth2UserRequest.getClientRegistration().getRegistrationId().toUpperCase()))
                .providerId(oAuth2UserInfo.getId())
                .actif(true)
                .roles(new HashSet<>())
                .build();

        // Assigner le rôle CLIENT par défaut
        Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                .orElseThrow(() -> new RuntimeException("Rôle CLIENT non trouvé"));
        user.getRoles().add(clientRole);

        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2UserInfo oAuth2UserInfo) {
        log.info("Mise à jour de l'utilisateur OAuth2: {}", existingUser.getEmail());

        if (oAuth2UserInfo.getFirstName() != null) {
            existingUser.setPrenom(oAuth2UserInfo.getFirstName());
        }
        if (oAuth2UserInfo.getLastName() != null) {
            existingUser.setNom(oAuth2UserInfo.getLastName());
        }

        return userRepository.save(existingUser);
    }

    private String generateUsername(String email) {
        String baseUsername = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }
}