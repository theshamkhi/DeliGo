//package com.shamkhi.deligo.application.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.shamkhi.deligo.domain.livraison.dto.LivreurDTO;
//import com.shamkhi.deligo.domain.livraison.model.Livreur;
//import com.shamkhi.deligo.domain.livraison.model.Zone;
//import com.shamkhi.deligo.domain.livraison.repository.LivreurRepository;
//import com.shamkhi.deligo.domain.livraison.repository.ZoneRepository;
//import org.junit.jupiter.api.*;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//
//import static org.hamcrest.Matchers.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//@Transactional
//@DisplayName("Tests d'intégration du LivreurController")
//class LivreurControllerIntegrationTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//    @Autowired private LivreurRepository livreurRepository;
//    @Autowired private ZoneRepository zoneRepository;
//
//    private Zone zone;
//
//    @BeforeEach
//    void setUp() {
//        livreurRepository.deleteAll();
//        zoneRepository.deleteAll();
//
//        zone = new Zone();
//        zone.setNom("Zone Centre");
//        zone.setCodePostal("20000");
//        zone.setVille("Casablanca");
//        zone = zoneRepository.save(zone);
//    }
//
//    @Nested
//    @DisplayName("Tests GET /livreurs")
//    class GetAllLivreursTests {
//        @Test
//        @DisplayName("Devrait retourner une liste paginée vide")
//        void shouldReturnEmptyPagedList() throws Exception {
//            mockMvc.perform(get("/livreurs")
//                            .param("page", "0")
//                            .param("size", "10"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isArray())
//                    .andExpect(jsonPath("$.content").isEmpty())
//                    .andExpect(jsonPath("$.totalElements").value(0));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner une liste paginée avec des livreurs")
//        void shouldReturnPagedListWithLivreurs() throws Exception {
//            createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//            createAndSaveLivreur("Bennani", "Karim", "0698765432");
//
//            mockMvc.perform(get("/livreurs")
//                            .param("page", "0")
//                            .param("size", "10"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isArray())
//                    .andExpect(jsonPath("$.content", hasSize(2)))
//                    .andExpect(jsonPath("$.totalElements").value(2));
//        }
//
//        @Test
//        @DisplayName("Devrait respecter la pagination")
//        void shouldRespectPagination() throws Exception {
//            for (int i = 1; i <= 5; i++) {
//                createAndSaveLivreur("Nom" + i, "Prenom" + i, "06123456" + i + i);
//            }
//
//            mockMvc.perform(get("/livreurs")
//                            .param("page", "0")
//                            .param("size", "2"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content", hasSize(2)))
//                    .andExpect(jsonPath("$.totalElements").value(5))
//                    .andExpect(jsonPath("$.totalPages").value(3));
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests GET /livreurs/actifs")
//    class GetActifLivreursTests {
//        @Test
//        @DisplayName("Devrait retourner uniquement les livreurs actifs")
//        void shouldReturnOnlyActifs() throws Exception {
//            Livreur actif1 = createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//            actif1.setActif(true);
//            livreurRepository.save(actif1);
//
//            Livreur actif2 = createAndSaveLivreur("Bennani", "Karim", "0698765432");
//            actif2.setActif(true);
//            livreurRepository.save(actif2);
//
//            Livreur inactif = createAndSaveLivreur("Idrissi", "Omar", "0687654321");
//            inactif.setActif(false);
//            livreurRepository.save(inactif);
//
//            mockMvc.perform(get("/livreurs/actifs"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$").isArray())
//                    .andExpect(jsonPath("$", hasSize(2)));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner liste vide si aucun livreur actif")
//        void shouldReturnEmptyWhenNoActifs() throws Exception {
//            Livreur inactif = createAndSaveLivreur("Idrissi", "Omar", "0687654321");
//            inactif.setActif(false);
//            livreurRepository.save(inactif);
//
//            mockMvc.perform(get("/livreurs/actifs"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$").isArray())
//                    .andExpect(jsonPath("$").isEmpty());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests POST /livreurs")
//    class CreateLivreurTests {
//        @Test
//        @DisplayName("Devrait créer un livreur et retourner 201")
//        void shouldCreateLivreur() throws Exception {
//            LivreurDTO request = createLivreurDTO("Alami", "Ahmed", "0612345678");
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.id").exists())
//                    .andExpect(jsonPath("$.nom").value("Alami"))
//                    .andExpect(jsonPath("$.prenom").value("Ahmed"))
//                    .andExpect(jsonPath("$.telephone").value("0612345678"))
//                    .andExpect(jsonPath("$.actif").value(true));
//        }
//
//        @Test
//        @DisplayName("Devrait créer un livreur avec zone assignée")
//        void shouldCreateWithZone() throws Exception {
//            LivreurDTO request = createLivreurDTO("Alami", "Ahmed", "0612345678");
//            request.setZoneAssigneeId(zone.getId());
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.id").exists());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si nom vide")
//        void shouldReturn400WhenNomEmpty() throws Exception {
//            LivreurDTO request = createLivreurDTO("", "Ahmed", "0612345678");
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si téléphone invalide")
//        void shouldReturn400WhenTelephoneInvalid() throws Exception {
//            LivreurDTO request = createLivreurDTO("Alami", "Ahmed", "123");
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 409 si téléphone existe déjà")
//        void shouldReturn409WhenTelephoneExists() throws Exception {
//            createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//
//            LivreurDTO request = createLivreurDTO("Bennani", "Karim", "0612345678");
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isConflict());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si zone non trouvée")
//        void shouldReturn404WhenZoneNotFound() throws Exception {
//            LivreurDTO request = createLivreurDTO("Alami", "Ahmed", "0612345678");
//            request.setZoneAssigneeId("invalid-zone-id");
//
//            mockMvc.perform(post("/livreurs")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests GET /livreurs/{id}")
//    class GetLivreurByIdTests {
//        @Test
//        @DisplayName("Devrait retourner un livreur par ID")
//        void shouldGetLivreurById() throws Exception {
//            String livreurId = createLivreurAndGetId();
//
//            mockMvc.perform(get("/livreurs/{id}", livreurId))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(livreurId))
//                    .andExpect(jsonPath("$.nom").value("Alami"));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si livreur non trouvé")
//        void shouldReturn404WhenNotFound() throws Exception {
//            mockMvc.perform(get("/livreurs/{id}", "invalid-id"))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests GET /livreurs/search")
//    class SearchLivreursTests {
//        @Test
//        @DisplayName("Devrait rechercher par nom")
//        void shouldSearchByNom() throws Exception {
//            createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//            createAndSaveLivreur("Bennani", "Karim", "0698765432");
//
//            mockMvc.perform(get("/livreurs/search")
//                            .param("keyword", "Alami"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isArray())
//                    .andExpect(jsonPath("$.content[*].nom", hasItem("Alami")));
//        }
//
//        @Test
//        @DisplayName("Devrait rechercher par téléphone")
//        void shouldSearchByTelephone() throws Exception {
//            createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//            createAndSaveLivreur("Bennani", "Karim", "0698765432");
//
//            mockMvc.perform(get("/livreurs/search")
//                            .param("keyword", "0612"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content", hasSize(1)));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner liste vide si aucun résultat")
//        void shouldReturnEmptyWhenNoResults() throws Exception {
//            createAndSaveLivreur("Alami", "Ahmed", "0612345678");
//
//            mockMvc.perform(get("/livreurs/search")
//                            .param("keyword", "NonExistant"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isEmpty());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests PUT /livreurs/{id}")
//    class UpdateLivreurTests {
//        @Test
//        @DisplayName("Devrait mettre à jour un livreur")
//        void shouldUpdateLivreur() throws Exception {
//            String livreurId = createLivreurAndGetId();
//
//            LivreurDTO updateRequest = createLivreurDTO("Alami-Updated", "Ahmed-Updated", "0612345679");
//
//            mockMvc.perform(put("/livreurs/{id}", livreurId)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(livreurId))
//                    .andExpect(jsonPath("$.nom").value("Alami-Updated"));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si livreur non trouvé")
//        void shouldReturn404WhenNotFound() throws Exception {
//            LivreurDTO updateRequest = createLivreurDTO("Alami", "Ahmed", "0612345678");
//
//            mockMvc.perform(put("/livreurs/{id}", "invalid-id")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si données invalides")
//        void shouldReturn400WhenInvalidData() throws Exception {
//            String livreurId = createLivreurAndGetId();
//
//            LivreurDTO updateRequest = createLivreurDTO("", "Ahmed", "0612345678");
//
//            mockMvc.perform(put("/livreurs/{id}", livreurId)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests DELETE /livreurs/{id}")
//    class DeleteLivreurTests {
//        @Test
//        @DisplayName("Devrait supprimer un livreur")
//        void shouldDeleteLivreur() throws Exception {
//            String livreurId = createLivreurAndGetId();
//
//            mockMvc.perform(delete("/livreurs/{id}", livreurId))
//                    .andDo(print())
//                    .andExpect(status().isNoContent());
//
//            mockMvc.perform(get("/livreurs/{id}", livreurId))
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si livreur non trouvé")
//        void shouldReturn404WhenNotFound() throws Exception {
//            mockMvc.perform(delete("/livreurs/{id}", "invalid-id"))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    // Méthodes helper
//    private Livreur createAndSaveLivreur(String nom, String prenom, String telephone) {
//        Livreur livreur = new Livreur();
//        livreur.setNom(nom);
//        livreur.setPrenom(prenom);
//        livreur.setTelephone(telephone);
//        livreur.setActif(true);
//        return livreurRepository.save(livreur);
//    }
//
//    private String createLivreurAndGetId() throws Exception {
//        LivreurDTO request = createLivreurDTO("Alami", "Ahmed", "0612345678");
//
//        String response = mockMvc.perform(post("/livreurs")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        return objectMapper.readTree(response).get("id").asText();
//    }
//
//    private LivreurDTO createLivreurDTO(String nom, String prenom, String telephone) {
//        LivreurDTO dto = new LivreurDTO();
//        dto.setNom(nom);
//        dto.setPrenom(prenom);
//        dto.setTelephone(telephone);
//        dto.setActif(true);
//        return dto;
//    }
//}