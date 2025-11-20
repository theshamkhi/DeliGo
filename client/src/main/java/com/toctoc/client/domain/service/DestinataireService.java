package com.toctoc.client.domain.service;

import com.toctoc.client.domain.model.Destinataire;
import com.toctoc.client.domain.repository.DestinataireRepository;
import com.toctoc.client.dto.DestinataireDTO;
import com.toctoc.client.mapper.DestinataireMapper;
import com.toctoc.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DestinataireService {

    private final DestinataireRepository repository;
    private final DestinataireMapper mapper;

    public Page<DestinataireDTO> getAllDestinataires(Pageable pageable) {
        log.info("Récupération de tous les destinataires");
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public DestinataireDTO getDestinataireById(String id) {
        log.info("Récupération du destinataire: {}", id);
        return mapper.toDTO(findDestinataireById(id));
    }

    public Page<DestinataireDTO> searchDestinataires(String keyword, Pageable pageable) {
        log.info("Recherche de destinataires avec: {}", keyword);
        return repository.searchByKeyword(keyword, pageable).map(mapper::toDTO);
    }

    @Transactional
    public DestinataireDTO createDestinataire(DestinataireDTO dto) {
        log.info("Création d'un destinataire");
        Destinataire destinataire = mapper.toEntity(dto);
        destinataire = repository.save(destinataire);
        log.info("Destinataire créé avec id: {}", destinataire.getId());
        return mapper.toDTO(destinataire);
    }

    @Transactional
    public DestinataireDTO updateDestinataire(String id, DestinataireDTO dto) {
        log.info("Mise à jour du destinataire: {}", id);
        Destinataire destinataire = findDestinataireById(id);
        mapper.updateEntity(dto, destinataire);
        destinataire = repository.save(destinataire);
        return mapper.toDTO(destinataire);
    }

    @Transactional
    public void deleteDestinataire(String id) {
        log.info("Suppression du destinataire: {}", id);
        Destinataire destinataire = findDestinataireById(id);
        repository.delete(destinataire);
    }

    private Destinataire findDestinataireById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Destinataire non trouvé avec id: " + id));
    }
}