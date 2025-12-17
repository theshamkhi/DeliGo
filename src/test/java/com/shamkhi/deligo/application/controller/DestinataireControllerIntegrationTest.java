package com.shamkhi.deligo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shamkhi.deligo.domain.client.dto.DestinataireDTO;
import com.shamkhi.deligo.domain.client.model.Destinataire;
import com.shamkhi.deligo.domain.client.repository.DestinataireRepository;
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
@DisplayName("Tests d'intégration du DestinataireController")
class DestinataireControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DestinataireRepository destinataireRepository;

    @BeforeEach
    void setUp() {
        destinataireRepository.deleteAll();
    }

    @Nested
    @DisplayName("Tests GET /destinataires")
    class GetAllDestinatairesTests {
        @Test
        @DisplayName("Devrait retourner une liste paginée vide")
        void shouldReturnEmptyPagedList() throws Exception {
            mockMvc.perform(get("/destinataires")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Devrait retourner une liste paginée avec des destinataires")
        void shouldReturnPagedListWithDestinataires() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");
            createAndSaveDestinataire("Dupont", "Jean", "0612345678");

            mockMvc.perform(get("/destinataires")
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
            for (int i = 1; i <= 5; i++) {
                createAndSaveDestinataire("Nom" + i, "Prenom" + i, "06987654" + i + i);
            }

            mockMvc.perform(get("/destinataires")
                            .param("page", "0")
                            .param("size", "2"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(2)))
                    .andExpect(jsonPath("$.totalElements").value(5))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }

        @Test
        @DisplayName("Devrait trier par nom par défaut")
        void shouldSortByNomByDefault() throws Exception {
            createAndSaveDestinataire("Zidane", "Zinedine", "0698765432");
            createAndSaveDestinataire("Alami", "Ahmed", "0612345678");

            mockMvc.perform(get("/destinataires"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].nom").value("Alami"))
                    .andExpect(jsonPath("$.content[1].nom").value("Zidane"));
        }
    }

    @Nested
    @DisplayName("Tests POST /destinataires")
    class CreateDestinataireTests {
        @Test
        @DisplayName("Devrait créer un destinataire et retourner 201")
        void shouldCreateDestinataire() throws Exception {
            DestinataireDTO request = createDestinataireDTO("Martin", "Marie", "0698765432", "456 Ave Test");

            mockMvc.perform(post("/destinataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nom").value("Martin"))
                    .andExpect(jsonPath("$.prenom").value("Marie"))
                    .andExpect(jsonPath("$.telephone").value("0698765432"));
        }

        @Test
        @DisplayName("Devrait retourner 400 si nom vide")
        void shouldReturn400WhenNomEmpty() throws Exception {
            DestinataireDTO request = createDestinataireDTO("", "Marie", "0698765432", "456 Ave Test");

            mockMvc.perform(post("/destinataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si prénom vide")
        void shouldReturn400WhenPrenomEmpty() throws Exception {
            DestinataireDTO request = createDestinataireDTO("Martin", "", "0698765432", "456 Ave Test");

            mockMvc.perform(post("/destinataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si téléphone invalide")
        void shouldReturn400WhenTelephoneInvalid() throws Exception {
            DestinataireDTO request = createDestinataireDTO("Martin", "Marie", "123", "456 Ave Test");

            mockMvc.perform(post("/destinataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si adresse vide")
        void shouldReturn400WhenAdresseEmpty() throws Exception {
            DestinataireDTO request = createDestinataireDTO("Martin", "Marie", "0698765432", "");

            mockMvc.perform(post("/destinataires")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests GET /destinataires/{id}")
    class GetDestinataireByIdTests {
        @Test
        @DisplayName("Devrait retourner un destinataire par ID")
        void shouldGetDestinataireById() throws Exception {
            String destinataireId = createDestinataireAndGetId();

            mockMvc.perform(get("/destinataires/{id}", destinataireId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(destinataireId))
                    .andExpect(jsonPath("$.nom").value("Martin"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si destinataire non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/destinataires/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests GET /destinataires/search")
    class SearchDestinatairesTests {
        @Test
        @DisplayName("Devrait rechercher par nom")
        void shouldSearchByNom() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");
            createAndSaveDestinataire("Dupont", "Jean", "0612345678");

            mockMvc.perform(get("/destinataires/search")
                            .param("keyword", "Martin"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].nom", hasItem("Martin")));
        }

        @Test
        @DisplayName("Devrait rechercher par prénom")
        void shouldSearchByPrenom() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");
            createAndSaveDestinataire("Dupont", "Jean", "0612345678");

            mockMvc.perform(get("/destinataires/search")
                            .param("keyword", "Marie"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].prenom").value("Marie"));
        }

        @Test
        @DisplayName("Devrait rechercher par téléphone")
        void shouldSearchByTelephone() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");
            createAndSaveDestinataire("Dupont", "Jean", "0612345678");

            mockMvc.perform(get("/destinataires/search")
                            .param("keyword", "0698"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)));
        }

        @Test
        @DisplayName("Devrait retourner liste vide si aucun résultat")
        void shouldReturnEmptyWhenNoResults() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");

            mockMvc.perform(get("/destinataires/search")
                            .param("keyword", "NonExistant"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("Devrait rechercher de manière insensible à la casse")
        void shouldSearchCaseInsensitive() throws Exception {
            createAndSaveDestinataire("Martin", "Marie", "0698765432");

            mockMvc.perform(get("/destinataires/search")
                            .param("keyword", "martin"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(greaterThan(0))));
        }
    }

    @Nested
    @DisplayName("Tests PUT /destinataires/{id}")
    class UpdateDestinataireTests {
        @Test
        @DisplayName("Devrait mettre à jour un destinataire")
        void shouldUpdateDestinataire() throws Exception {
            String destinataireId = createDestinataireAndGetId();

            DestinataireDTO updateRequest = createDestinataireDTO(
                    "Martin-Updated",
                    "Marie-Updated",
                    "0698765433",
                    "789 New Address"
            );

            mockMvc.perform(put("/destinataires/{id}", destinataireId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(destinataireId))
                    .andExpect(jsonPath("$.nom").value("Martin-Updated"))
                    .andExpect(jsonPath("$.prenom").value("Marie-Updated"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si destinataire non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            DestinataireDTO updateRequest = createDestinataireDTO("Martin", "Marie", "0698765432", "456 Ave Test");

            mockMvc.perform(put("/destinataires/{id}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 400 si données invalides")
        void shouldReturn400WhenInvalidData() throws Exception {
            String destinataireId = createDestinataireAndGetId();

            DestinataireDTO updateRequest = createDestinataireDTO("", "Marie", "0698765432", "456 Ave Test");

            mockMvc.perform(put("/destinataires/{id}", destinataireId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests DELETE /destinataires/{id}")
    class DeleteDestinataireTests {
        @Test
        @DisplayName("Devrait supprimer un destinataire")
        void shouldDeleteDestinataire() throws Exception {
            String destinataireId = createDestinataireAndGetId();

            mockMvc.perform(delete("/destinataires/{id}", destinataireId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/destinataires/{id}", destinataireId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 404 si destinataire non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/destinataires/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // Méthodes helper
    private Destinataire createAndSaveDestinataire(String nom, String prenom, String telephone) {
        Destinataire destinataire = new Destinataire();
        destinataire.setNom(nom);
        destinataire.setPrenom(prenom);
        destinataire.setTelephone(telephone);
        destinataire.setAdresse("123 Rue Test");
        return destinataireRepository.save(destinataire);
    }

    private String createDestinataireAndGetId() throws Exception {
        DestinataireDTO request = createDestinataireDTO("Martin", "Marie", "0698765432", "456 Ave Test");

        String response = mockMvc.perform(post("/destinataires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private DestinataireDTO createDestinataireDTO(String nom, String prenom, String telephone, String adresse) {
        DestinataireDTO dto = new DestinataireDTO();
        dto.setNom(nom);
        dto.setPrenom(prenom);
        dto.setTelephone(telephone);
        dto.setAdresse(adresse);
        return dto;
    }
}