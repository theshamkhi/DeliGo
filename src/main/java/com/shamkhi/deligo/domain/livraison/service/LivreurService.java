package com.shamkhi.deligo.domain.livraison.service;

import com.shamkhi.deligo.application.mapper.LivreurMapper;
import com.shamkhi.deligo.domain.livraison.dto.LivreurDTO;
import com.shamkhi.deligo.domain.livraison.model.Livreur;
import com.shamkhi.deligo.domain.livraison.repository.LivreurRepository;
import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.RoleRepository;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
import com.shamkhi.deligo.infrastructure.exception.DuplicateResourceException;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LivreurService {

    private final LivreurRepository repository;
    private final ZoneRepository zoneRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final LivreurMapper mapper;

    public Page<LivreurDTO> getAllLivreurs(Pageable pageable) {
        log.info("Récupération de tous les livreurs");
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public List<LivreurDTO> getActiveLivreurs() {
        log.info("Récupération des livreurs actifs");
        return mapper.toDTOList(repository.findByActif(true));
    }

    public LivreurDTO getLivreurById(String id) {
        log.info("Récupération du livreur: {}", id);
        Livreur livreur = findLivreurById(id);
        LivreurDTO dto = mapper.toDTO(livreur);

        // Ajouter les informations du user associé
        userRepository.findByLivreurId(id).ifPresent(user -> {
            dto.setUserId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
        });

        return dto;
    }

    public Page<LivreurDTO> searchLivreurs(String keyword, Pageable pageable) {
        log.info("Recherche de livreurs avec: {}", keyword);
        return repository.searchByKeyword(keyword, pageable).map(livreur -> {
            LivreurDTO dto = mapper.toDTO(livreur);
            userRepository.findByLivreurId(livreur.getId()).ifPresent(user -> {
                dto.setUserId(user.getId());
                dto.setUsername(user.getUsername());
                dto.setEmail(user.getEmail());
            });
            return dto;
        });
    }

    @Transactional
    public LivreurDTO createLivreur(LivreurDTO dto) {
        log.info("Création d'un livreur avec création automatique du compte utilisateur");

        // Vérifications d'unicité
        validateUniqueConstraints(dto);

        // 1. Créer le Livreur
        Livreur livreur = mapper.toEntity(dto);

        if (dto.getZoneAssigneeId() != null) {
            livreur.setZoneAssignee(zoneRepository.findById(dto.getZoneAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        }

        livreur = repository.save(livreur);
        log.info("Livreur créé avec id: {}", livreur.getId());

        // 2. Créer automatiquement le User associé
        User user = createUserForLivreur(livreur, dto);
        user = userRepository.save(user);
        log.info("Compte utilisateur créé avec id: {} pour le livreur: {}", user.getId(), livreur.getId());

        // 3. Mettre à jour le DTO avec l'ID du user
        LivreurDTO result = mapper.toDTO(livreur);
        result.setUserId(user.getId());
        result.setUsername(user.getUsername());
        result.setEmail(user.getEmail());

        return result;
    }

    @Transactional
    public LivreurDTO updateLivreur(String id, LivreurDTO dto) {
        log.info("Mise à jour du livreur: {}", id);

        Livreur livreur = findLivreurById(id);

        // Vérifier l'unicité du téléphone si changé
        if (!livreur.getTelephone().equals(dto.getTelephone()) &&
                repository.existsByTelephone(dto.getTelephone())) {
            throw new DuplicateResourceException("Un livreur avec ce téléphone existe déjà");
        }

        mapper.updateEntity(dto, livreur);

        if (dto.getZoneAssigneeId() != null) {
            livreur.setZoneAssignee(zoneRepository.findById(dto.getZoneAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        } else {
            livreur.setZoneAssignee(null);
        }

        livreur = repository.save(livreur);

        // Mettre à jour aussi le User associé si nécessaire
        updateAssociatedUser(livreur, dto);

        LivreurDTO result = mapper.toDTO(livreur);
        userRepository.findByLivreurId(id).ifPresent(user -> {
            result.setUserId(user.getId());
            result.setUsername(user.getUsername());
            result.setEmail(user.getEmail());
        });

        return result;
    }

    @Transactional
    public void deleteLivreur(String id) {
        log.info("Suppression du livreur: {}", id);
        Livreur livreur = findLivreurById(id);

        // Supprimer aussi le User associé
        userRepository.findByLivreurId(id).ifPresent(user -> {
            log.info("Suppression du compte utilisateur associé: {}", user.getId());
            userRepository.delete(user);
        });

        repository.delete(livreur);
        log.info("Livreur et compte utilisateur supprimés avec succès");
    }

    @Transactional
    public void activateLivreur(String id) {
        log.info("Activation du livreur: {}", id);
        Livreur livreur = findLivreurById(id);
        livreur.setActif(true);
        repository.save(livreur);

        // Activer aussi le compte utilisateur
        userRepository.findByLivreurId(id).ifPresent(user -> {
            user.setActif(true);
            userRepository.save(user);
            log.info("Compte utilisateur activé: {}", user.getId());
        });
    }

    @Transactional
    public void deactivateLivreur(String id) {
        log.info("Désactivation du livreur: {}", id);
        Livreur livreur = findLivreurById(id);
        livreur.setActif(false);
        repository.save(livreur);

        // Désactiver aussi le compte utilisateur
        userRepository.findByLivreurId(id).ifPresent(user -> {
            user.setActif(false);
            userRepository.save(user);
            log.info("Compte utilisateur désactivé: {}", user.getId());
        });
    }

    private void validateUniqueConstraints(LivreurDTO dto) {
        if (repository.existsByTelephone(dto.getTelephone())) {
            throw new DuplicateResourceException("Un livreur avec ce téléphone existe déjà");
        }

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Cet email existe déjà");
        }
    }

    private User createUserForLivreur(Livreur livreur, LivreurDTO dto) {
        // Récupérer le rôle LIVREUR
        Role livreurRole = roleRepository.findByName("ROLE_LIVREUR")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle LIVREUR non trouvé"));

        // Créer le User
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nom(livreur.getNom())
                .prenom(livreur.getPrenom())
                .telephone(livreur.getTelephone())
                .livreur(livreur)
                .actif(livreur.getActif() != null ? livreur.getActif() : true)
                .roles(new HashSet<>())
                .build();

        user.getRoles().add(livreurRole);

        return user;
    }

    private void updateAssociatedUser(Livreur livreur, LivreurDTO dto) {
        userRepository.findByLivreurId(livreur.getId()).ifPresent(user -> {
            log.info("Mise à jour du compte utilisateur associé: {}", user.getId());

            // Mettre à jour les informations du user
            user.setNom(livreur.getNom());
            user.setPrenom(livreur.getPrenom());
            user.setTelephone(livreur.getTelephone());

            // Mettre à jour l'email si fourni et différent
            if (dto.getEmail() != null && !dto.getEmail().equals(user.getEmail())) {
                if (userRepository.existsByEmail(dto.getEmail())) {
                    throw new DuplicateResourceException("Cet email existe déjà");
                }
                user.setEmail(dto.getEmail());
            }

            // Mettre à jour le username si fourni et différent
            if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
                if (userRepository.existsByUsername(dto.getUsername())) {
                    throw new DuplicateResourceException("Ce nom d'utilisateur existe déjà");
                }
                user.setUsername(dto.getUsername());
            }

            // Mettre à jour le mot de passe si fourni
            if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            userRepository.save(user);
        });
    }

    private Livreur findLivreurById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec id: " + id));
    }
}