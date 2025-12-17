package com.shamkhi.deligo.domain.security.service;

import com.shamkhi.deligo.application.mapper.SecurityMapper;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final SecurityMapper securityMapper;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        log.info("Tentative de connexion pour l'utilisateur: {}", request.getUsername());

        // Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Générer le token JWT
        String token = jwtService.generateToken(userDetails);

        // Récupérer les informations complètes de l'utilisateur
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // Extraire les rôles et permissions
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .collect(Collectors.toSet());

        log.info("Connexion réussie pour l'utilisateur: {}", request.getUsername());

        return LoginResponse.builder()
                .token(token)
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
    public UserDTO register(RegisterRequest request) {
        log.info("Création d'un nouvel utilisateur: {}", request.getUsername());

        // Vérifier l'unicité du username et email
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email existe déjà");
        }

        // Créer l'utilisateur
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nom(request.getNom())
                .prenom(request.getPrenom())
                .telephone(request.getTelephone())
                .actif(true)
                .clientExpediteurId(request.getClientExpediteurId())
                .livreurId(request.getLivreurId())
                .roles(new HashSet<>())
                .build();

        // Assigner les rôles
        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<Role> roles = request.getRoles().stream()
                    .map(roleName -> roleRepository.findByName(roleName)
                            .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + roleName)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        } else {
            // Par défaut, assigner le rôle CLIENT
            Role clientRole = roleRepository.findByName("ROLE_CLIENT")
                    .orElseThrow(() -> new ResourceNotFoundException("Rôle CLIENT non trouvé"));
            user.getRoles().add(clientRole);
        }

        user = userRepository.save(user);
        log.info("Utilisateur créé avec succès: {}", user.getUsername());

        return securityMapper.toUserDTO(user);
    }

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

        // Vérifier l'unicité du username et email
        if (!user.getUsername().equals(request.getUsername()) &&
                userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        if (!user.getEmail().equals(request.getEmail()) &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email existe déjà");
        }

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setNom(request.getNom());
        user.setPrenom(request.getPrenom());
        user.setTelephone(request.getTelephone());
        user.setClientExpediteurId(request.getClientExpediteurId());
        user.setLivreurId(request.getLivreurId());

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

        user = userRepository.save(user);
        return securityMapper.toUserDTO(user);
    }

    @Transactional
    public void deleteUser(String id) {
        log.info("Suppression de l'utilisateur: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        userRepository.delete(user);
    }

    @Transactional
    public void activateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setActif(true);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setActif(false);
        userRepository.save(user);
    }

}