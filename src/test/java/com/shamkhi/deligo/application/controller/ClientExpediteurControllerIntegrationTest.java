package com.shamkhi.deligo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shamkhi.deligo.domain.client.dto.ClientExpediteurDTO;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration du ClientExpediteurController")
class ClientExpediteurControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ClientExpediteurRepository clientRepository;

    @BeforeEach
    void setUp() {
        clientRepository.deleteAll();
    }

    @Nested
    @DisplayName("Tests GET /clients")
    class GetAllClientsTests {
        @Test
        @DisplayName("Devrait retourner une liste paginée vide")
        void shouldReturnEmptyPagedList() throws Exception {
            mockMvc.perform(get("/clients")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Devrait retourner une liste paginée avec des clients")
        void shouldReturnPagedListWithClients() throws Exception {
            // Créer des clients de test
            createAndSaveClient("Dupont", "Jean", "jean@test.com");
            createAndSaveClient("Martin", "Marie", "marie@test.com");

            mockMvc.perform(get("/clients")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        @DisplayName("Devrait respecter la pagination")
        void shouldRespectPagination() throws Exception {
            // Créer 5 clients
            for (int i = 1; i <= 5; i++) {
                createAndSaveClient("Nom" + i, "Prenom" + i, "email" + i + "@test.com");
            }

            mockMvc.perform(get("/clients")
                            .param("page", "0")
                            .param("size", "2"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }
    }

    @Nested
    @DisplayName("Tests POST /clients")
    class CreateClientTests {
        @Test
        @DisplayName("Devrait créer un client et retourner 201")
        void shouldCreateClient() throws Exception {
            ClientExpediteurDTO request = createClientDTO(
                    "Dupont",
                    "Jean",
                    "jean@test.com",
                    "0612345678",
                    "123 Rue Test"
            );

            mockMvc.perform(post("/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nom").value("Dupont"))
                    .andExpect(jsonPath("$.prenom").value("Jean"))
                    .andExpect(jsonPath("$.email").value("jean@test.com"))
                    .andExpect(jsonPath("$.telephone").value("0612345678"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si nom vide")
        void shouldReturn400WhenNomEmpty() throws Exception {
            ClientExpediteurDTO request = createClientDTO(
                    "",  // Nom vide
                    "Jean",
                    "jean@test.com",
                    "0612345678",
                    "123 Rue Test"
            );

            mockMvc.perform(post("/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si email invalide")
        void shouldReturn400WhenEmailInvalid() throws Exception {
            ClientExpediteurDTO request = createClientDTO(
                    "Dupont",
                    "Jean",
                    "invalid-email",  // Email invalide
                    "0612345678",
                    "123 Rue Test"
            );

            mockMvc.perform(post("/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si téléphone invalide")
        void shouldReturn400WhenTelephoneInvalid() throws Exception {
            ClientExpediteurDTO request = createClientDTO(
                    "Dupont",
                    "Jean",
                    "jean@test.com",
                    "123",  // Téléphone invalide
                    "123 Rue Test"
            );

            mockMvc.perform(post("/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 409 si email existe déjà")
        void shouldReturn409WhenEmailExists() throws Exception {
            // Créer un client existant
            createAndSaveClient("Dupont", "Jean", "jean@test.com");

            // Tenter de créer un autre client avec le même email
            ClientExpediteurDTO request = createClientDTO(
                    "Martin",
                    "Marie",
                    "jean@test.com",  // Email déjà existant
                    "0698765432",
                    "456 Ave Test"
            );

            mockMvc.perform(post("/clients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Tests GET /clients/{id}")
    class GetClientByIdTests {
        @Test
        @DisplayName("Devrait retourner un client par ID")
        void shouldGetClientById() throws Exception {
            String clientId = createClientAndGetId();

            mockMvc.perform(get("/clients/{id}", clientId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(clientId))
                    .andExpect(jsonPath("$.nom").value("Dupont"))
                    .andExpect(jsonPath("$.email").value("jean@test.com"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si client non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/clients/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests GET /clients/search")
    class SearchClientsTests {
        @Test
        @DisplayName("Devrait rechercher par nom")
        void shouldSearchByNom() throws Exception {
            createAndSaveClient("Dupont", "Jean", "jean@test.com");
            createAndSaveClient("Martin", "Marie", "marie@test.com");
            createAndSaveClient("Durand", "Pierre", "pierre@test.com");

            mockMvc.perform(get("/clients/search")
                            .param("keyword", "Dupont"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].nom", hasItem("Dupont")));
        }

        @Test
        @DisplayName("Devrait rechercher par email")
        void shouldSearchByEmail() throws Exception {
            createAndSaveClient("Dupont", "Jean", "jean@test.com");
            createAndSaveClient("Martin", "Marie", "marie@test.com");

            mockMvc.perform(get("/clients/search")
                            .param("keyword", "marie@test.com"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].email").value("marie@test.com"));
        }

        @Test
        @DisplayName("Devrait retourner liste vide si aucun résultat")
        void shouldReturnEmptyWhenNoResults() throws Exception {
            createAndSaveClient("Dupont", "Jean", "jean@test.com");

            mockMvc.perform(get("/clients/search")
                            .param("keyword", "NonExistant"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("Devrait rechercher de manière insensible à la casse")
        void shouldSearchCaseInsensitive() throws Exception {
            createAndSaveClient("Dupont", "Jean", "jean@test.com");

            mockMvc.perform(get("/clients/search")
                            .param("keyword", "dupont"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }
    }

    @Nested
    @DisplayName("Tests PUT /clients/{id}")
    class UpdateClientTests {
        @Test
        @DisplayName("Devrait mettre à jour un client")
        void shouldUpdateClient() throws Exception {
            String clientId = createClientAndGetId();

            ClientExpediteurDTO updateRequest = createClientDTO(
                    "Dupont-Updated",
                    "Jean-Updated",
                    "jean.updated@test.com",
                    "0612345679",
                    "456 New Address"
            );

            mockMvc.perform(put("/clients/{id}", clientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(clientId))
                    .andExpect(jsonPath("$.nom").value("Dupont-Updated"))
                    .andExpect(jsonPath("$.prenom").value("Jean-Updated"))
                    .andExpect(jsonPath("$.email").value("jean.updated@test.com"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si client non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            ClientExpediteurDTO updateRequest = createClientDTO(
                    "Dupont",
                    "Jean",
                    "jean@test.com",
                    "0612345678",
                    "123 Rue Test"
            );

            mockMvc.perform(put("/clients/{id}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 400 si données invalides")
        void shouldReturn400WhenInvalidData() throws Exception {
            String clientId = createClientAndGetId();

            ClientExpediteurDTO updateRequest = createClientDTO(
                    "",  // Nom vide
                    "Jean",
                    "jean@test.com",
                    "0612345678",
                    "123 Rue Test"
            );

            mockMvc.perform(put("/clients/{id}", clientId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests DELETE /clients/{id}")
    class DeleteClientTests {
        @Test
        @DisplayName("Devrait supprimer un client")
        void shouldDeleteClient() throws Exception {
            String clientId = createClientAndGetId();

            mockMvc.perform(delete("/clients/{id}", clientId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Vérifier que le client n'existe plus
            mockMvc.perform(get("/clients/{id}", clientId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 404 si client non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/clients/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // Méthodes helper
    private ClientExpediteur createAndSaveClient(String nom, String prenom, String email) {
        ClientExpediteur client = new ClientExpediteur();
        client.setNom(nom);
        client.setPrenom(prenom);
        client.setEmail(email);
        client.setTelephone("0612345678");
        client.setAdresse("123 Rue Test");
        return clientRepository.save(client);
    }

    private String createClientAndGetId() throws Exception {
        ClientExpediteurDTO request = createClientDTO(
                "Dupont",
                "Jean",
                "jean@test.com",
                "0612345678",
                "123 Rue Test"
        );

        String response = mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private ClientExpediteurDTO createClientDTO(
            String nom,
            String prenom,
            String email,
            String telephone,
            String adresse
    ) {
        ClientExpediteurDTO dto = new ClientExpediteurDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setEmail(email);
        dto.setTelephone(telephone);
        dto.setAdresse(adresse);
        return dto;
    }
}