package com.shamkhi.deligo.application.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shamkhi.deligo.domain.produit.dto.ProduitDTO;
import com.shamkhi.deligo.domain.produit.model.Produit;
import com.shamkhi.deligo.domain.produit.repository.ProduitRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Tests d'intégration du ProduitController")
class ProduitControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ProduitRepository produitRepository;

    @BeforeEach
    void setUp() {
        produitRepository.deleteAll();
    }

    @Nested
    @DisplayName("Tests GET /produits")
    class GetAllProduitsTests {
        @Test
        @DisplayName("Devrait retourner une liste paginée vide")
        void shouldReturnEmptyPagedList() throws Exception {
            mockMvc.perform(get("/produits")
                            .param("page", "0")
                            .param("size", "10"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        @DisplayName("Devrait retourner une liste paginée avec des produits")
        void shouldReturnPagedListWithProduits() throws Exception {
            createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
            createAndSaveProduit("iPhone 13", "Électronique", new BigDecimal("8000"), new BigDecimal("0.2"));

            mockMvc.perform(get("/produits")
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
                createAndSaveProduit("Produit" + i, "Catégorie" + i, new BigDecimal("100" + i), new BigDecimal("1.0"));
            }

            mockMvc.perform(get("/produits")
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
    @DisplayName("Tests POST /produits")
    class CreateProduitTests {
        @Test
        @DisplayName("Devrait créer un produit et retourner 201")
        void shouldCreateProduit() throws Exception {
            ProduitDTO request = createProduitDTO("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(post("/produits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").exists())
                    .andExpect(jsonPath("$.nom").value("Laptop Dell"))
                    .andExpect(jsonPath("$.categorie").value("Électronique"))
                    .andExpect(jsonPath("$.prix").value(5000))
                    .andExpect(jsonPath("$.poids").value(2.5));
        }

        @Test
        @DisplayName("Devrait retourner 400 si nom vide")
        void shouldReturn400WhenNomEmpty() throws Exception {
            ProduitDTO request = createProduitDTO("", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(post("/produits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si catégorie vide")
        void shouldReturn400WhenCategorieEmpty() throws Exception {
            ProduitDTO request = createProduitDTO("Laptop Dell", "", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(post("/produits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Devrait retourner 400 si prix négatif")
        void shouldReturn400WhenPrixNegatif() throws Exception {
            ProduitDTO request = createProduitDTO("Laptop Dell", "Électronique", new BigDecimal("-100"), new BigDecimal("2.5"));

            mockMvc.perform(post("/produits")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests GET /produits/{id}")
    class GetProduitByIdTests {
        @Test
        @DisplayName("Devrait retourner un produit par ID")
        void shouldGetProduitById() throws Exception {
            String produitId = createProduitAndGetId();

            mockMvc.perform(get("/produits/{id}", produitId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(produitId))
                    .andExpect(jsonPath("$.nom").value("Laptop Dell"));
        }

        @Test
        @DisplayName("Devrait retourner 404 si produit non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/produits/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Tests GET /produits/search")
    class SearchProduitsTests {
        @Test
        @DisplayName("Devrait rechercher par nom")
        void shouldSearchByNom() throws Exception {
            createAndSaveProduit("Laptop Dell XPS", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
            createAndSaveProduit("iPhone 13", "Mobile", new BigDecimal("8000"), new BigDecimal("0.2"));

            mockMvc.perform(get("/produits/search")
                            .param("keyword", "Dell"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[*].nom", hasItem(containsString("Dell"))));
        }

        @Test
        @DisplayName("Devrait rechercher par catégorie")
        void shouldSearchByCategorie() throws Exception {
            createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
            createAndSaveProduit("T-Shirt Nike", "Vêtements", new BigDecimal("200"), new BigDecimal("0.3"));

            mockMvc.perform(get("/produits/search")
                            .param("keyword", "Vêtements"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].categorie").value("Vêtements"));
        }

        @Test
        @DisplayName("Devrait retourner liste vide si aucun résultat")
        void shouldReturnEmptyWhenNoResults() throws Exception {
            createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(get("/produits/search")
                            .param("keyword", "NonExistant"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }
    }

    @Nested
    @DisplayName("Tests PUT /produits/{id}")
    class UpdateProduitTests {
        @Test
        @DisplayName("Devrait mettre à jour un produit")
        void shouldUpdateProduit() throws Exception {
            String produitId = createProduitAndGetId();

            ProduitDTO updateRequest = createProduitDTO(
                    "Laptop Dell Updated",
                    "Ordinateur",
                    new BigDecimal("5500"),
                    new BigDecimal("2.8")
            );

            mockMvc.perform(put("/produits/{id}", produitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(produitId))
                    .andExpect(jsonPath("$.nom").value("Laptop Dell Updated"))
                    .andExpect(jsonPath("$.categorie").value("Ordinateur"))
                    .andExpect(jsonPath("$.prix").value(5500));
        }

        @Test
        @DisplayName("Devrait retourner 404 si produit non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            ProduitDTO updateRequest = createProduitDTO("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(put("/produits/{id}", "invalid-id")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 400 si données invalides")
        void shouldReturn400WhenInvalidData() throws Exception {
            String produitId = createProduitAndGetId();

            ProduitDTO updateRequest = createProduitDTO("", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

            mockMvc.perform(put("/produits/{id}", produitId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Tests DELETE /produits/{id}")
    class DeleteProduitTests {
        @Test
        @DisplayName("Devrait supprimer un produit")
        void shouldDeleteProduit() throws Exception {
            String produitId = createProduitAndGetId();

            mockMvc.perform(delete("/produits/{id}", produitId))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/produits/{id}", produitId))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Devrait retourner 404 si produit non trouvé")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/produits/{id}", "invalid-id"))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    private Produit createAndSaveProduit(String nom, String categorie, BigDecimal prix, BigDecimal poids) {
        Produit produit = new Produit();
        produit.setNom(nom);
        produit.setCategorie(categorie);
        produit.setPrix(prix);
        produit.setPoids(poids);
        return produitRepository.save(produit);
    }

    private String createProduitAndGetId() throws Exception {
        ProduitDTO request = createProduitDTO("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

        String response = mockMvc.perform(post("/produits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asText();
    }

    private ProduitDTO createProduitDTO(String nom, String categorie, BigDecimal prix, BigDecimal poids) {
        ProduitDTO dto = new ProduitDTO();
        dto.setNom(nom);
        dto.setCategorie(categorie);
        dto.setPrix(prix);
        dto.setPoids(poids);
        return dto;
    }
}