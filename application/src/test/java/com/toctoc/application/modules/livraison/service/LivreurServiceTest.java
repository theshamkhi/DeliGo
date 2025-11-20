package com.toctoc.application.modules.livraison.service;

import com.toctoc.livraison.domain.service.LivreurService;
import com.toctoc.livraison.dto.LivreurDTO;
import com.toctoc.livraison.domain.model.Livreur;
import com.toctoc.livraison.domain.model.Zone;
import com.toctoc.livraison.domain.repository.LivreurRepository;
import com.toctoc.livraison.domain.repository.ZoneRepository;
import com.toctoc.common.exception.DuplicateResourceException;
import com.toctoc.common.exception.ResourceNotFoundException;
import com.toctoc.livraison.mapper.LivreurMapper;
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
@DisplayName("Tests du LivreurService")
class LivreurServiceTest {

    @Mock private LivreurRepository repository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private LivreurMapper mapper;
    @InjectMocks private LivreurService service;

    private Livreur livreur;
    private LivreurDTO livreurDTO;
    private Zone zone;

    @BeforeEach
    void setUp() {
        zone = new Zone();
        zone.setId("zone-1");
        zone.setNom("Zone Centre");

        livreur = new Livreur();
        livreur.setId("1");
        livreur.setNom("Alami");
        livreur.setPrenom("Ahmed");
        livreur.setTelephone("0612345678");
        livreur.setActif(true);

        livreurDTO = new LivreurDTO();
        livreurDTO.setId("1");
        livreurDTO.setNom("Alami");
        livreurDTO.setTelephone("0612345678");
        livreurDTO.setActif(true);
    }

    @Nested
    @DisplayName("Tests de récupération")
    class GetTests {
        @Test
        @DisplayName("Devrait récupérer tous les livreurs")
        void shouldGetAll() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Livreur> page = new PageImpl<>(Arrays.asList(livreur));
            when(repository.findAll(pageable)).thenReturn(page);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            Page<LivreurDTO> result = service.getAllLivreurs(pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Devrait récupérer les livreurs actifs")
        void shouldGetActifs() {
            when(repository.findByActif(true)).thenReturn(Arrays.asList(livreur));
            when(mapper.toDTOList(any())).thenReturn(Arrays.asList(livreurDTO));

            List<LivreurDTO> result = service.getActiveLivreurs();

            assertThat(result).hasSize(1);
            verify(repository).findByActif(true);
        }

        @Test
        @DisplayName("Devrait récupérer un livreur par ID")
        void shouldGetById() {
            when(repository.findById("1")).thenReturn(Optional.of(livreur));
            when(mapper.toDTO(livreur)).thenReturn(livreurDTO);

            LivreurDTO result = service.getLivreurById("1");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("1");
        }

        @Test
        @DisplayName("Devrait lever exception si non trouvé")
        void shouldThrowWhenNotFound() {
            when(repository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.getLivreurById("invalid"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests de création")
    class CreateTests {
        @Test
        @DisplayName("Devrait créer un livreur")
        void shouldCreate() {
            when(repository.existsByTelephone(anyString())).thenReturn(false);
            when(mapper.toEntity(livreurDTO)).thenReturn(livreur);
            when(repository.save(any())).thenReturn(livreur);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            LivreurDTO result = service.createLivreur(livreurDTO);

            assertThat(result).isNotNull();
            verify(repository).save(any());
        }

        @Test
        @DisplayName("Devrait créer avec zone assignée")
        void shouldCreateWithZone() {
            livreurDTO.setZoneAssigneeId("zone-1");

            when(repository.existsByTelephone(anyString())).thenReturn(false);
            when(mapper.toEntity(livreurDTO)).thenReturn(livreur);
            when(zoneRepository.findById("zone-1")).thenReturn(Optional.of(zone));
            when(repository.save(any())).thenReturn(livreur);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            LivreurDTO result = service.createLivreur(livreurDTO);

            assertThat(result).isNotNull();
            verify(zoneRepository).findById("zone-1");
        }

        @Test
        @DisplayName("Devrait lever exception si téléphone existe")
        void shouldThrowWhenTelephoneExists() {
            when(repository.existsByTelephone("0612345678")).thenReturn(true);

            assertThatThrownBy(() -> service.createLivreur(livreurDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }

        @Test
        @DisplayName("Devrait lever exception si zone non trouvée")
        void shouldThrowWhenZoneNotFound() {
            livreurDTO.setZoneAssigneeId("invalid");

            when(repository.existsByTelephone(anyString())).thenReturn(false);
            when(mapper.toEntity(livreurDTO)).thenReturn(livreur);
            when(zoneRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.createLivreur(livreurDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Tests de mise à jour")
    class UpdateTests {
        @Test
        @DisplayName("Devrait mettre à jour un livreur")
        void shouldUpdate() {
            when(repository.findById("1")).thenReturn(Optional.of(livreur));
            when(repository.save(any())).thenReturn(livreur);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            LivreurDTO result = service.updateLivreur("1", livreurDTO);

            assertThat(result).isNotNull();
            verify(mapper).updateEntity(livreurDTO, livreur);
        }

        @Test
        @DisplayName("Devrait permettre de garder le même téléphone")
        void shouldAllowSameTelephone() {
            when(repository.findById("1")).thenReturn(Optional.of(livreur));
            when(repository.save(any())).thenReturn(livreur);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            service.updateLivreur("1", livreurDTO);

            verify(repository, never()).existsByTelephone(anyString());
        }

        @Test
        @DisplayName("Devrait lever exception si nouveau téléphone existe")
        void shouldThrowWhenNewTelephoneExists() {
            livreurDTO.setTelephone("0698765432");

            when(repository.findById("1")).thenReturn(Optional.of(livreur));
            when(repository.existsByTelephone("0698765432")).thenReturn(true);

            assertThatThrownBy(() -> service.updateLivreur("1", livreurDTO))
                    .isInstanceOf(DuplicateResourceException.class);
        }
    }

    @Nested
    @DisplayName("Tests de suppression")
    class DeleteTests {
        @Test
        @DisplayName("Devrait supprimer un livreur")
        void shouldDelete() {
            when(repository.findById("1")).thenReturn(Optional.of(livreur));

            service.deleteLivreur("1");

            verify(repository).delete(livreur);
        }
    }

    @Nested
    @DisplayName("Tests de recherche")
    class SearchTests {
        @Test
        @DisplayName("Devrait rechercher par mot-clé")
        void shouldSearch() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Livreur> page = new PageImpl<>(Arrays.asList(livreur));
            when(repository.searchByKeyword("Alami", pageable)).thenReturn(page);
            when(mapper.toDTO(any())).thenReturn(livreurDTO);

            Page<LivreurDTO> result = service.searchLivreurs("Alami", pageable);

            assertThat(result).isNotEmpty();
        }
    }
}