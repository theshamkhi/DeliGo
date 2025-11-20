package com.toctoc.livraison.domain.service;

import com.toctoc.common.exception.ResourceNotFoundException;
import com.toctoc.livraison.domain.model.Zone;
import com.toctoc.livraison.domain.repository.ZoneRepository;
import com.toctoc.livraison.dto.ZoneDTO;
import com.toctoc.livraison.mapper.ZoneMapper;
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
public class ZoneService {

    private final ZoneRepository repository;
    private final ZoneMapper mapper;

    public Page<ZoneDTO> getAllZones(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toDTO);
    }

    public ZoneDTO getZoneById(String id) {
        return mapper.toDTO(findZoneById(id));
    }

    public Page<ZoneDTO> searchZones(String keyword, Pageable pageable) {
        return repository.searchByKeyword(keyword, pageable).map(mapper::toDTO);
    }

    @Transactional
    public ZoneDTO createZone(ZoneDTO dto) {
        Zone zone = mapper.toEntity(dto);
        zone = repository.save(zone);
        return mapper.toDTO(zone);
    }

    @Transactional
    public ZoneDTO updateZone(String id, ZoneDTO dto) {
        Zone zone = findZoneById(id);
        mapper.updateEntity(dto, zone);
        zone = repository.save(zone);
        return mapper.toDTO(zone);
    }

    @Transactional
    public void deleteZone(String id) {
        Zone zone = findZoneById(id);
        repository.delete(zone);
    }

    private Zone findZoneById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone non trouvée avec id: " + id));
    }
}