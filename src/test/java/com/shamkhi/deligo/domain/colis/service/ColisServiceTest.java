package com.shamkhi.deligo.domain.colis.service;

import com.shamkhi.deligo.application.mapper.ColisMapper;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.domain.client.repository.DestinataireRepository;
import com.shamkhi.deligo.domain.colis.dto.ColisDTO;
import com.shamkhi.deligo.domain.colis.dto.CreateColisRequest;
import com.shamkhi.deligo.domain.colis.dto.UpdateColisRequest;
import com.shamkhi.deligo.domain.colis.dto.UpdateStatutRequest;
import com.shamkhi.deligo.domain.colis.model.Colis;
import com.shamkhi.deligo.domain.colis.model.HistoriqueLivraison;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.model.StatutColis;
import com.shamkhi.deligo.domain.colis.repository.ColisProduitRepository;
import com.shamkhi.deligo.domain.colis.repository.ColisRepository;
import com.shamkhi.deligo.domain.colis.repository.HistoriqueLivraisonRepository;
import com.shamkhi.deligo.domain.livraison.repository.LivreurRepository;
import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.*;
import com.shamkhi.deligo.domain.colis.repository.*;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.model.Destinataire;
import com.shamkhi.deligo.domain.client.repository.*;
import com.shamkhi.deligo.domain.livraison.repository.*;
import com.shamkhi.deligo.domain.produit.repository.ProduitRepository;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests du ColisService")
class ColisServiceTest {

    @Mock private ColisRepository colisRepository;
    @Mock private HistoriqueLivraisonRepository historiqueRepository;
    @Mock private ColisProduitRepository colisProduitRepository;
    @Mock private ClientExpediteurRepository clientRepository;
    @Mock private DestinataireRepository destinataireRepository;
    @Mock private LivreurRepository livreurRepository;
    @Mock private ZoneRepository zoneRepository;
    @Mock private ProduitRepository produitRepository;
    @Mock private ColisMapper colisMapper;

    @InjectMocks
    private ColisService colisService;

    private Colis colis;
    private ColisDTO colisDTO;
    private CreateColisRequest createRequest;
    private ClientExpediteur client;
    private Destinataire destinataire;

    @BeforeEach
    void setUp() {
        client = new ClientExpediteur();
        client.setId("client-1");
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setEmail("jean@test.com");

        destinataire = new Destinataire();
        destinataire.setId("dest-1");
        destinataire.setNom("Martin");
        destinataire.setPrenom("Marie");

        colis = new Colis();
        colis.setId("colis-1");
        colis.setDescription("Laptop Dell");
        colis.setPoids(BigDecimal.valueOf(2.5));
        colis.setStatut(StatutColis.CREE);
        colis.setPriorite(PrioriteColis.NORMALE);
        colis.setVilleDestination("Rabat");
        colis.setClientExpediteur(client);
        colis.setDestinataire(destinataire);

        colisDTO = new ColisDTO();
        colisDTO.setId("colis-1");
        colisDTO.setDescription("Laptop Dell");

        createRequest = new CreateColisRequest();
        createRequest.setDescription("Laptop Dell");
        createRequest.setPoids(BigDecimal.valueOf(2.5));
        createRequest.setPriorite(PrioriteColis.NORMALE);
        createRequest.setVilleDestination("Rabat");
        createRequest.setClientExpediteurId("client-1");
        createRequest.setDestinataireId("dest-1");
    }

    @Nested
    @DisplayName("Tests de création")
    class CreateTests {
        @Test
        @DisplayName("Devrait créer un colis avec succès")
        void shouldCreateColis() {
            when(clientRepository.findById("client-1")).thenReturn(Optional.of(client));
            when(destinataireRepository.findById("dest-1")).thenReturn(Optional.of(destinataire));
            when(colisMapper.toEntity(createRequest)).thenReturn(colis);
            when(colisRepository.save(any(Colis.class))).thenReturn(colis);
            when(colisMapper.toDTO(colis)).thenReturn(colisDTO);

            ColisDTO result = colisService.createColis(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("colis-1");
            verify(colisRepository).save(any(Colis.class));
            verify(historiqueRepository).save(any(HistoriqueLivraison.class));
        }

        @Test
        @DisplayName("Devrait lever une exception si client non trouvé")
        void shouldThrowWhenClientNotFound() {
            when(clientRepository.findById("client-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> colisService.createColis(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Client expéditeur non trouvé");
        }

        @Test
        @DisplayName("Devrait lever une exception si destinataire non trouvé")
        void shouldThrowWhenDestinataireNotFound() {
            when(clientRepository.findById("client-1")).thenReturn(Optional.of(client));
            when(destinataireRepository.findById("dest-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> colisService.createColis(createRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Destinataire non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests de récupération")
    class GetTests {
        @Test
        @DisplayName("Devrait récupérer tous les colis")
        void shouldGetAllColis() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Colis> page = new PageImpl<>(Arrays.asList(colis));
            when(colisRepository.findAll(pageable)).thenReturn(page);
            when(colisMapper.toDTO(any())).thenReturn(colisDTO);

            Page<ColisDTO> result = colisService.getAllColis(pageable);

            assertThat(result).isNotEmpty();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Devrait récupérer un colis par ID")
        void shouldGetColisById() {
            when(colisRepository.findById("colis-1")).thenReturn(Optional.of(colis));
            when(colisMapper.toDTO(colis)).thenReturn(colisDTO);

            ColisDTO result = colisService.getColisById("colis-1");

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("colis-1");
        }

        @Test
        @DisplayName("Devrait lever une exception si colis non trouvé")
        void shouldThrowWhenColisNotFound() {
            when(colisRepository.findById("invalid")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> colisService.getColisById("invalid"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("non trouvé");
        }
    }

    @Nested
    @DisplayName("Tests de mise à jour")
    class UpdateTests {
        @Test
        @DisplayName("Devrait mettre à jour un colis")
        void shouldUpdateColis() {
            UpdateColisRequest updateRequest = new UpdateColisRequest();
            updateRequest.setDescription("Updated");

            when(colisRepository.findById("colis-1")).thenReturn(Optional.of(colis));
            when(colisRepository.save(any())).thenReturn(colis);
            when(colisMapper.toDTO(any())).thenReturn(colisDTO);

            ColisDTO result = colisService.updateColis("colis-1", updateRequest);

            assertThat(result).isNotNull();
            verify(colisRepository).save(any());
        }

        @Test
        @DisplayName("Devrait mettre à jour le statut")
        void shouldUpdateStatut() {
            UpdateStatutRequest statutRequest = new UpdateStatutRequest();
            statutRequest.setStatut(StatutColis.COLLECTE);
            statutRequest.setCommentaire("Test");

            when(colisRepository.findById("colis-1")).thenReturn(Optional.of(colis));

            colisService.updateStatut("colis-1", statutRequest);

            verify(colisRepository).save(any());
            verify(historiqueRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Tests de suppression")
    class DeleteTests {
        @Test
        @DisplayName("Devrait supprimer un colis")
        void shouldDeleteColis() {
            when(colisRepository.findById("colis-1")).thenReturn(Optional.of(colis));

            colisService.deleteColis("colis-1");

            verify(colisRepository).delete(colis);
        }
    }

    @Nested
    @DisplayName("Tests de recherche")
    class SearchTests {
        @Test
        @DisplayName("Devrait rechercher par mot-clé")
        void shouldSearchByKeyword() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Colis> page = new PageImpl<>(Arrays.asList(colis));
            when(colisRepository.searchByKeyword("Dell", pageable)).thenReturn(page);
            when(colisMapper.toDTO(any())).thenReturn(colisDTO);

            Page<ColisDTO> result = colisService.searchColis("Dell", pageable);

            assertThat(result).isNotEmpty();
        }
    }
}