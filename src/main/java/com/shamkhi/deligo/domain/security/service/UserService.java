package com.shamkhi.deligo.domain.security.service;

import com.shamkhi.deligo.application.mapper.SecurityMapper;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.domain.livraison.model.Livreur;
import com.shamkhi.deligo.domain.livraison.model.Zone;
import com.shamkhi.deligo.domain.livraison.repository.LivreurRepository;
import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
import com.shamkhi.deligo.domain.security.dto.*;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.RoleRepository;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
import com.shamkhi.deligo.infrastructure.exception.DuplicateResourceException;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ClientExpediteurRepository clientExpediteurRepository;
    private final LivreurRepository livreurRepository;
    private final ZoneRepository zoneRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityMapper securityMapper;
    private final UserDetailsService userDetailsService;

    // ========== Authentication Methods ==========

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'utilisateur: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        log.info("Connexion réussie pour l'utilisateur: {}", request.getUsername());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .type("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nom(user.getNom())
                .prenom(user.getPrenom())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public TokenRefreshResponse refreshToken(String refreshToken) {
        log.info("Tentative de rafraîchissement du token");

        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("Refresh token invalide ou expiré");
        }

        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        String newAccessToken = jwtService.generateToken(userDetails);
        String newRefreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("Token rafraîchi avec succès pour l'utilisateur: {}", username);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .type("Bearer")
                .build();
    }

    // ========== Unified Registration (All User Types) ==========

    /**
     * Unified registration endpoint for all user types.
     * Automatically creates linked business entities (Livreur/Client) based on roles.
     *
     * @param request Registration request with user credentials and optional business data
     * @return UserDTO with all linked entity IDs
     */
    @Transactional
    public UserDTO register(RegisterRequest request) {
        log.info("Création d'un utilisateur: {} avec rôles: {}",
                request.getUsername(), request.getRoles());

        // Validate unique constraints
        validateUniqueConstraints(request);

        // Validate business data requirements
        validateBusinessDataRequirements(request);

        // Create User entity
        User user = createUserEntity(request);

        // Auto-create and link business entities based on roles
        if (hasRole(request, "ROLE_LIVREUR")) {
            Livreur livreur = createLivreurEntity(request);
            livreur = livreurRepository.save(livreur);
            user.setLivreur(livreur);
            log.info("Livreur créé et lié: {}", livreur.getId());
        }

        if (hasRole(request, "ROLE_CLIENT")) {
            ClientExpediteur client = createClientEntity(request);
            client = clientExpediteurRepository.save(client);
            user.setClientExpediteur(client);
            log.info("Client créé et lié: {}", client.getId());
        }

        user = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", user.getId());

        return securityMapper.toUserDTO(user);
    }

    // ========== User Management Methods ==========

    public UserDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return securityMapper.toUserDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return securityMapper.toUserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(securityMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    public List<UserDTO> getUsersByRole(String roleName) {
        return userRepository.findByRoleName(roleName).stream()
                .map(securityMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDTO updateUser(String id, RegisterRequest request) {
        log.info("Mise à jour de l'utilisateur: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Validate unique constraints (excluding current user)
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email existe déjà");
        }

        // Update user basic info
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        // Update linked entities if they exist
        if (user.getLivreur() != null && request.getLivreurData() != null) {
            updateLivreurEntity(user.getLivreur(), request);
        }

        if (user.getClientExpediteur() != null && request.getClientData() != null) {
            updateClientEntity(user.getClientExpediteur(), request);
        }

        user = userRepository.save(user);
        return securityMapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(String id) {
        log.info("Suppression de l'utilisateur: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Cascade deletion will handle linked Livreur/Client
        userRepository.delete(user);
    }

    @Transactional
    public void activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setActif(true);

        // Also activate linked entities
        if (user.getLivreur() != null) {
            user.getLivreur().setActif(true);
        }

        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setActif(false);

        // Also deactivate linked entities
        if (user.getLivreur() != null) {
            user.getLivreur().setActif(false);
        }

        userRepository.save(user);
    }

    // ========== Private Helper Methods ==========

    private void validateUniqueConstraints(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email existe déjà");
        }

        // Validate business entity unique constraints
        if (hasRole(request, "ROLE_LIVREUR") && request.getTelephone() != null) {
            if (livreurRepository.existsByTelephone(request.getTelephone())) {
                throw new DuplicateResourceException("Un livreur avec ce téléphone existe déjà");
            }
        }

        if (hasRole(request, "ROLE_CLIENT")) {
            if (clientExpediteurRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Un client avec cet email existe déjà");
            }
        }
    }

    private void validateBusinessDataRequirements(RegisterRequest request) {
        if (hasRole(request, "ROLE_LIVREUR") && request.getLivreurData() == null) {
            throw new IllegalArgumentException(
                    "livreurData est requis pour créer un utilisateur avec le rôle LIVREUR");
        }

        if (hasRole(request, "ROLE_CLIENT") && request.getClientData() == null) {
            throw new IllegalArgumentException(
                    "clientData est requis pour créer un utilisateur avec le rôle CLIENT");
        }
    }

    private User createUserEntity(RegisterRequest request) {
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName)))
                .collect(Collectors.toSet());

        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .actif(true)
                .roles(roles)
                .build();
    }

    private Livreur createLivreurEntity(RegisterRequest request) {
        RegisterRequest.LivreurCreationData data = request.getLivreurData();

        Livreur livreur = new Livreur();
        livreur.setNom(request.getNom());
        livreur.setPrenom(request.getPrenom());
        livreur.setTelephone(request.getTelephone());
        livreur.setVehicule(data.getVehicule());
        livreur.setActif(data.getActif() != null ? data.getActif() : true);

        if (data.getZoneAssigneeId() != null) {
            Zone zone = zoneRepository.findById(data.getZoneAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée"));
            livreur.setZoneAssignee(zone);
        }

        return livreur;
    }

    private ClientExpediteur createClientEntity(RegisterRequest request) {
        RegisterRequest.ClientCreationData data = request.getClientData();

        ClientExpediteur client = new ClientExpediteur();
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setEmail(request.getEmail());
        client.setTelephone(request.getTelephone());
        client.setAdresse(data.getAdresse());

        return client;
    }

    private void updateLivreurEntity(Livreur livreur, RegisterRequest request) {
        livreur.setNom(request.getNom());
        livreur.setPrenom(request.getPrenom());
        livreur.setTelephone(request.getTelephone());

        if (request.getLivreurData() != null) {
            RegisterRequest.LivreurCreationData data = request.getLivreurData();
            livreur.setVehicule(data.getVehicule());
            livreur.setActif(data.getActif());

            if (data.getZoneAssigneeId() != null) {
                Zone zone = zoneRepository.findById(data.getZoneAssigneeId())
                        .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée"));
                livreur.setZoneAssignee(zone);
            } else {
                livreur.setZoneAssignee(null);
            }
        }
    }

    private void updateClientEntity(ClientExpediteur client, RegisterRequest request) {
        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setEmail(request.getEmail());
        client.setTelephone(request.getTelephone());

        if (request.getClientData() != null) {
            client.setAdresse(request.getClientData().getAdresse());
        }
    }

    private boolean hasRole(RegisterRequest request, String roleName) {
        return request.getRoles() != null && request.getRoles().contains(roleName);
    }
}