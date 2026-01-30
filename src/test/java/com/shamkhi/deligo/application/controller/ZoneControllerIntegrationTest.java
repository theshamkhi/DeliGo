//package com.shamkhi.deligo.application.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.shamkhi.deligo.domain.livraison.dto.ZoneDTO;
//import com.shamkhi.deligo.domain.livraison.model.Zone;
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
//@DisplayName("Tests d'intégration du ZoneController")
//class ZoneControllerIntegrationTest {
//
//    @Autowired private MockMvc mockMvc;
//    @Autowired private ObjectMapper objectMapper;
//    @Autowired private ZoneRepository zoneRepository;
//
//    @BeforeEach
//    void setUp() {
//        zoneRepository.deleteAll();
//    }
//
//    @Nested
//    @DisplayName("Tests GET /zones")
//    class GetAllZonesTests {
//        @Test
//        @DisplayName("Devrait retourner une liste paginée vide")
//        void shouldReturnEmptyPagedList() throws Exception {
//            mockMvc.perform(get("/zones")
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
//        @DisplayName("Devrait retourner une liste paginée avec des zones")
//        void shouldReturnPagedListWithZones() throws Exception {
//            createAndSaveZone("Centre", "20000", "Casablanca");
//            createAndSaveZone("Agdal", "10000", "Rabat");
//
//            mockMvc.perform(get("/zones")
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
//                createAndSaveZone("Zone" + i, "2000" + i, "Ville" + i);
//            }
//
//            mockMvc.perform(get("/zones")
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
//    @DisplayName("Tests POST /zones")
//    class CreateZoneTests {
//        @Test
//        @DisplayName("Devrait créer une zone et retourner 201")
//        void shouldCreateZone() throws Exception {
//            ZoneDTO request = createZoneDTO("Centre", "20000", "Casablanca");
//
//            mockMvc.perform(post("/zones")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isCreated())
//                    .andExpect(jsonPath("$.id").exists())
//                    .andExpect(jsonPath("$.nom").value("Centre"))
//                    .andExpect(jsonPath("$.codePostal").value("20000"))
//                    .andExpect(jsonPath("$.ville").value("Casablanca"));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si nom vide")
//        void shouldReturn400WhenNomEmpty() throws Exception {
//            ZoneDTO request = createZoneDTO("", "20000", "Casablanca");
//
//            mockMvc.perform(post("/zones")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si code postal vide")
//        void shouldReturn400WhenCodePostalEmpty() throws Exception {
//            ZoneDTO request = createZoneDTO("Centre", "", "Casablanca");
//
//            mockMvc.perform(post("/zones")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si ville vide")
//        void shouldReturn400WhenVilleEmpty() throws Exception {
//            ZoneDTO request = createZoneDTO("Centre", "20000", "");
//
//            mockMvc.perform(post("/zones")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(request)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests GET /zones/{id}")
//    class GetZoneByIdTests {
//        @Test
//        @DisplayName("Devrait retourner une zone par ID")
//        void shouldGetZoneById() throws Exception {
//            String zoneId = createZoneAndGetId();
//
//            mockMvc.perform(get("/zones/{id}", zoneId))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(zoneId))
//                    .andExpect(jsonPath("$.nom").value("Centre"));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si zone non trouvée")
//        void shouldReturn404WhenNotFound() throws Exception {
//            mockMvc.perform(get("/zones/{id}", "invalid-id"))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests GET /zones/search")
//    class SearchZonesTests {
//        @Test
//        @DisplayName("Devrait rechercher par nom")
//        void shouldSearchByNom() throws Exception {
//            createAndSaveZone("Centre-Ville", "20000", "Casablanca");
//            createAndSaveZone("Agdal", "10000", "Rabat");
//
//            mockMvc.perform(get("/zones/search")
//                            .param("keyword", "Centre"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isArray())
//                    .andExpect(jsonPath("$.content[*].nom", hasItem("Centre-Ville")));
//        }
//
//        @Test
//        @DisplayName("Devrait rechercher par code postal")
//        void shouldSearchByCodePostal() throws Exception {
//            createAndSaveZone("Centre", "20000", "Casablanca");
//            createAndSaveZone("Agdal", "10000", "Rabat");
//
//            mockMvc.perform(get("/zones/search")
//                            .param("keyword", "20000"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content", hasSize(1)));
//        }
//
//        @Test
//        @DisplayName("Devrait rechercher par ville")
//        void shouldSearchByVille() throws Exception {
//            createAndSaveZone("Centre", "20000", "Casablanca");
//            createAndSaveZone("Agdal", "10000", "Rabat");
//
//            mockMvc.perform(get("/zones/search")
//                            .param("keyword", "Rabat"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content", hasSize(1)));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner liste vide si aucun résultat")
//        void shouldReturnEmptyWhenNoResults() throws Exception {
//            createAndSaveZone("Centre", "20000", "Casablanca");
//
//            mockMvc.perform(get("/zones/search")
//                            .param("keyword", "NonExistant"))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.content").isEmpty());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests PUT /zones/{id}")
//    class UpdateZoneTests {
//        @Test
//        @DisplayName("Devrait mettre à jour une zone")
//        void shouldUpdateZone() throws Exception {
//            String zoneId = createZoneAndGetId();
//
//            ZoneDTO updateRequest = createZoneDTO("Centre-Updated", "20001", "Casa-Updated");
//
//            mockMvc.perform(put("/zones/{id}", zoneId)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.id").value(zoneId))
//                    .andExpect(jsonPath("$.nom").value("Centre-Updated"))
//                    .andExpect(jsonPath("$.codePostal").value("20001"));
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si zone non trouvée")
//        void shouldReturn404WhenNotFound() throws Exception {
//            ZoneDTO updateRequest = createZoneDTO("Centre", "20000", "Casablanca");
//
//            mockMvc.perform(put("/zones/{id}", "invalid-id")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 400 si données invalides")
//        void shouldReturn400WhenInvalidData() throws Exception {
//            String zoneId = createZoneAndGetId();
//
//            ZoneDTO updateRequest = createZoneDTO("", "20000", "Casablanca");
//
//            mockMvc.perform(put("/zones/{id}", zoneId)
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(updateRequest)))
//                    .andDo(print())
//                    .andExpect(status().isBadRequest());
//        }
//    }
//
//    @Nested
//    @DisplayName("Tests DELETE /zones/{id}")
//    class DeleteZoneTests {
//        @Test
//        @DisplayName("Devrait supprimer une zone")
//        void shouldDeleteZone() throws Exception {
//            String zoneId = createZoneAndGetId();
//
//            mockMvc.perform(delete("/zones/{id}", zoneId))
//                    .andDo(print())
//                    .andExpect(status().isNoContent());
//
//            mockMvc.perform(get("/zones/{id}", zoneId))
//                    .andExpect(status().isNotFound());
//        }
//
//        @Test
//        @DisplayName("Devrait retourner 404 si zone non trouvée")
//        void shouldReturn404WhenNotFound() throws Exception {
//            mockMvc.perform(delete("/zones/{id}", "invalid-id"))
//                    .andDo(print())
//                    .andExpect(status().isNotFound());
//        }
//    }
//
//    // Méthodes helper
//    private Zone createAndSaveZone(String nom, String codePostal, String ville) {
//        Zone zone = new Zone();
//        zone.setNom(nom);
//        zone.setCodePostal(codePostal);
//        zone.setVille(ville);
//        return zoneRepository.save(zone);
//    }
//
//    private String createZoneAndGetId() throws Exception {
//        ZoneDTO request = createZoneDTO("Centre", "20000", "Casablanca");
//
//        String response = mockMvc.perform(post("/zones")
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
//    private ZoneDTO createZoneDTO(String nom, String codePostal, String ville) {
//        ZoneDTO dto = new ZoneDTO();
//        dto.setNom(nom);
//        dto.setCodePostal(codePostal);
//        dto.setVille(ville);
//        return dto;
//    }
//}