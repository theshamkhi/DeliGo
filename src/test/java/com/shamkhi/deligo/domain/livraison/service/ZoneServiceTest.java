package com.shamkhi.deligo.domain.livraison.service;

import com.shamkhi.deligo.application.mapper.ZoneMapper;
import com.shamkhi.deligo.domain.livraison.dto.ZoneDTO;
import com.shamkhi.deligo.domain.livraison.model.Zone;
import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du ZoneService")
class ZoneServiceTest {

    @Mock private ZoneRepository repository;
    @Mock private ZoneMapper mapper;
    @InjectMocks private ZoneService service;

    private Zone zone;
    private ZoneDTO zoneDTO;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("1");
        zone.setNom("Centre");
        zone.setCodePostal("20000");
        zone.setVille("Casablanca");

        zoneDTO = new ZoneDTO();
        zoneDTO.setId("1");
        zoneDTO.setNom("Centre");
        zoneDTO.setCodePostal("20000");
    }

    @Test
    @DisplayName("Devrait récupérer toutes les zones")
    void shouldGetAll() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Zone> page = new PageImpl<>(Arrays.asList(zone));
        when(repository.findAll(pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(zoneDTO);

        Page<ZoneDTO> result = service.getAllZones(pageable);

        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Devrait récupérer une zone par ID")
    void shouldGetById() {
        when(repository.findById("1")).thenReturn(Optional.of(zone));
        when(mapper.toDTO(zone)).thenReturn(zoneDTO);

        ZoneDTO result = service.getZoneById("1");

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Devrait créer une zone")
    void shouldCreate() {
        when(mapper.toEntity(zoneDTO)).thenReturn(zone);
        when(repository.save(any())).thenReturn(zone);
        when(mapper.toDTO(any())).thenReturn(zoneDTO);

        ZoneDTO result = service.createZone(zoneDTO);

        assertThat(result).isNotNull();
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Devrait mettre à jour une zone")
    void shouldUpdate() {
        when(repository.findById("1")).thenReturn(Optional.of(zone));
        when(repository.save(any())).thenReturn(zone);
        when(mapper.toDTO(any())).thenReturn(zoneDTO);

        ZoneDTO result = service.updateZone("1", zoneDTO);

        assertThat(result).isNotNull();
        verify(mapper).updateEntity(zoneDTO, zone);
    }

    @Test
    @DisplayName("Devrait supprimer une zone")
    void shouldDelete() {
        when(repository.findById("1")).thenReturn(Optional.of(zone));

        service.deleteZone("1");

        verify(repository).delete(zone);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé")
    void shouldSearch() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Zone> page = new PageImpl<>(Arrays.asList(zone));
        when(repository.searchByKeyword("Centre", pageable)).thenReturn(page);
        when(mapper.toDTO(any())).thenReturn(zoneDTO);

        Page<ZoneDTO> result = service.searchZones("Centre", pageable);

        assertThat(result).isNotEmpty();
    }
}