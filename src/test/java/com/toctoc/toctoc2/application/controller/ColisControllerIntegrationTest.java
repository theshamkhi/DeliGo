package com.toctoc.toctoc2.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.toctoc.toctoc2.domain.colis.dto.*;
import com.toctoc.toctoc2.domain.colis.model.*;
import com.toctoc.toctoc2.domain.colis.repository.ColisRepository;
import com.toctoc.toctoc2.domain.client.model.*;
import com.toctoc.toctoc2.domain.client.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration du ColisController")
class ColisControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ColisRepository colisRepository;
    @Autowired private ClientExpediteurRepository clientRepository;
    @Autowired private DestinataireRepository destinataireRepository;

    private ClientExpediteur client;
    private Destinataire destinataire;

    @BeforeEach
    void setUp() {
        colisRepository.deleteAll();
        clientRepository.deleteAll();
        destinataireRepository.deleteAll();

        client = new ClientExpediteur();
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setEmail("jean@test.com");
        client.setTelephone("0612345678");
        client.setAdresse("123 Rue Test");
        client = clientRepository.save(client);

        destinataire = new Destinataire();
        destinataire.setNom("Martin");
        destinataire.setPrenom("Marie");
        destinataire.setTelephone("0698765432");
        destinataire.setAdresse("456 Ave Test");
        destinataire = destinataireRepository.save(destinataire);
    }

    @Nested
    @DisplayName("Tests GET /colis")
    class GetAllColisTests {
        @Test
        @DisplayName("Devrait retourner une liste paginée")
        void shouldReturnPagedList() throws Exception {
            mockMvc.perform(get("/colis")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    @Nested
    @DisplayName("Tests POST /colis")
    class CreateColisTests {
        @Test
        @DisplayName("Devrait créer un colis et retourner 201")
        void shouldCreateColis() throws Exception {
            CreateColisRequest request = new CreateColisRequest();
            request.setDescription("Laptop Dell");
            request.setPoids(BigDecimal.valueOf(2.5));
            request.setPriorite(PrioriteColis.NORMALE);
            request.setVilleDestination("Rabat");
            request.setClientExpediteurId(client.getId());
            request.setDestinataireId(destinataire.getId());

            mockMvc.perform(post("/colis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.description").value("Laptop Dell"))
                    .andExpect(jsonPath("$.statut").value("CREE"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si description vide")
        void shouldReturn400WhenDescriptionEmpty() throws Exception {
            CreateColisRequest request = new CreateColisRequest();
            request.setDescription(""); // Invalide
            request.setPoids(BigDecimal.valueOf(2.5));
            request.setPriorite(PrioriteColis.NORMALE);
            request.setVilleDestination("Rabat");
            request.setClientExpediteurId(client.getId());
            request.setDestinataireId(destinataire.getId());

            mockMvc.perform(post("/colis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 404 si client inexistant")
        void shouldReturn404WhenClientNotFound() throws Exception {
            CreateColisRequest request = new CreateColisRequest();
            request.setDescription("Test");
            request.setPoids(BigDecimal.valueOf(2.5));
            request.setPriorite(PrioriteColis.NORMALE);
            request.setVilleDestination("Rabat");
            request.setClientExpediteurId("invalid-id");
            request.setDestinataireId(destinataire.getId());

            mockMvc.perform(post("/colis")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests GET /colis/{id}")
    class GetColisByIdTests {
        @Test
        @DisplayName("Devrait retourner un colis par ID")
        void shouldGetColisById() throws Exception {
            String colisId = createColisAndGetId();

            mockMvc.perform(get("/colis/{id}", colisId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(colisId));
        }

        @Test
        @DisplayName("Devrait retourner 404 si non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/colis/{id}", "invalid-id"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests PATCH /colis/{id}/statut")
    class UpdateStatutTests {
        @Test
        @DisplayName("Devrait mettre à jour le statut")
        void shouldUpdateStatut() throws Exception {
            String colisId = createColisAndGetId();

            UpdateStatutRequest request = new UpdateStatutRequest();
            request.setStatut(StatutColis.COLLECTE);
            request.setCommentaire("Test update");

            mockMvc.perform(patch("/colis/{id}/statut", colisId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent());

            // Vérifier que le statut a changé
            mockMvc.perform(get("/colis/{id}", colisId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.statut").value("COLLECTE"));
        }
    }

    @Nested
    @DisplayName("Tests DELETE /colis/{id}")
    class DeleteColisTests {
        @Test
        @DisplayName("Devrait supprimer un colis")
        void shouldDeleteColis() throws Exception {
            String colisId = createColisAndGetId();

            mockMvc.perform(delete("/colis/{id}", colisId))
                    .andExpect(status().isNoContent());

            // Vérifier que le colis n'existe plus
            mockMvc.perform(get("/colis/{id}", colisId))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests GET /colis/search")
    class SearchColisTests {
        @Test
        @DisplayName("Devrait rechercher par mot-clé")
        void shouldSearchByKeyword() throws Exception {
            createColisAndGetId();

            mockMvc.perform(get("/colis/search")
                            .param("keyword", "Dell"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }

    // Méthode helper
    private String createColisAndGetId() throws Exception {
        CreateColisRequest request = new CreateColisRequest();
        request.setDescription("Laptop Dell");
        request.setPoids(BigDecimal.valueOf(2.5));
        request.setPriorite(PrioriteColis.NORMALE);
        request.setVilleDestination("Rabat");
        request.setClientExpediteurId(client.getId());
        request.setDestinataireId(destinataire.getId());

        String response = mockMvc.perform(post("/colis")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }
}