package com.toctoc.toctoc2.domain.livraison.service;

import com.toctoc.toctoc2.application.mapper.LivreurMapper;
import com.toctoc.toctoc2.domain.livraison.dto.LivreurDTO;
import com.toctoc.toctoc2.domain.livraison.model.Livreur;
import com.toctoc.toctoc2.domain.livraison.repository.LivreurRepository;
import com.toctoc.toctoc2.domain.livraison.repository.ZoneRepository;
import com.toctoc.toctoc2.infrastructure.exception.DuplicateResourceException;
import com.toctoc.toctoc2.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LivreurService {

    private final LivreurRepository repository;
    private final ZoneRepository zoneRepository;
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
        return mapper.toDTO(findLivreurById(id));
    }

    public Page<LivreurDTO> searchLivreurs(String keyword, Pageable pageable) {
        log.info("Recherche de livreurs avec: {}", keyword);
        return repository.searchByKeyword(keyword, pageable).map(mapper::toDTO);
    }

    @Transactional
    public LivreurDTO createLivreur(LivreurDTO dto) {
        log.info("Création d'un livreur");

        if (repository.existsByTelephone(dto.getTelephone())) {
            throw new DuplicateResourceException("Un livreur avec ce téléphone existe déjà");
        }

        Livreur livreur = mapper.toEntity(dto);

        if (dto.getZoneAssigneeId() != null) {
            livreur.setZoneAssignee(zoneRepository.findById(dto.getZoneAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        }

        livreur = repository.save(livreur);
        log.info("Livreur créé avec id: {}", livreur.getId());
        return mapper.toDTO(livreur);
    }

    @Transactional
    public LivreurDTO updateLivreur(String id, LivreurDTO dto) {
        log.info("Mise à jour du livreur: {}", id);

        Livreur livreur = findLivreurById(id);

        if (!livreur.getTelephone().equals(dto.getTelephone()) &&
                repository.existsByTelephone(dto.getTelephone())) {
            throw new DuplicateResourceException("Un livreur avec ce téléphone existe déjà");
        }

        mapper.updateEntity(dto, livreur);

        if (dto.getZoneAssigneeId() != null) {
            livreur.setZoneAssignee(zoneRepository.findById(dto.getZoneAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée")));
        }

        livreur = repository.save(livreur);
        return mapper.toDTO(livreur);
    }

    @Transactional
    public void deleteLivreur(String id) {
        log.info("Suppression du livreur: {}", id);
        Livreur livreur = findLivreurById(id);
        repository.delete(livreur);
    }

    private Livreur findLivreurById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur non trouvé avec id: " + id));
    }
}