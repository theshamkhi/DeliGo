package com.shamkhi.deligo.domain.client.service;

import com.shamkhi.deligo.application.mapper.ClientExpediteurMapper;
import com.shamkhi.deligo.domain.client.dto.ClientExpediteurDTO;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import com.shamkhi.deligo.infrastructure.exception.DuplicateResourceException;
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
public class ClientExpediteurService {

    private final ClientExpediteurRepository repository;
    private final ClientExpediteurMapper mapper;

    public Page<ClientExpediteurDTO> getAllClients(Pageable pageable) {
        log.info("Récupération de tous les clients expéditeurs");
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ClientExpediteurDTO getClientById(String id) {
        log.info("Récupération du client: {}", id);
        return mapper.toDTO(findClientById(id));
    }

    public Page<ClientExpediteurDTO> searchClients(String keyword, Pageable pageable) {
        log.info("Recherche de clients avec: {}", keyword);
        return repository.searchByKeyword(keyword, pageable).map(mapper::toDTO);
    }

    @Transactional
    public ClientExpediteurDTO createClient(ClientExpediteurDTO dto) {
        log.info("Création d'un client expéditeur");

        if (repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Un client avec cet email existe déjà");
        }

        ClientExpediteur client = mapper.toEntity(dto);
        client = repository.save(client);
        log.info("Client créé avec id: {}", client.getId());
        return mapper.toDTO(client);
    }

    @Transactional
    public ClientExpediteurDTO updateClient(String id, ClientExpediteurDTO dto) {
        log.info("Mise à jour du client: {}", id);

        ClientExpediteur client = findClientById(id);

        if (!client.getEmail().equals(dto.getEmail()) && repository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Un client avec cet email existe déjà");
        }

        mapper.updateEntity(dto, client);
        client = repository.save(client);
        log.info("Client mis à jour");
        return mapper.toDTO(client);
    }

    @Transactional
    public void deleteClient(String id) {
        log.info("Suppression du client: {}", id);
        ClientExpediteur client = findClientById(id);
        repository.delete(client);
    }

    private ClientExpediteur findClientById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec id: " + id));
    }
}