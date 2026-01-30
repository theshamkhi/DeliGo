package com.shamkhi.deligo.domain.produit.repository;

import com.shamkhi.deligo.domain.produit.model.Produit;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du ProduitRepository")
class ProduitRepositoryTest {

    @Autowired private ProduitRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un produit")
    void shouldSaveAndRetrieve() {
        Produit produit = createProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
        Produit saved = repository.save(produit);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo("Laptop Dell");

        Optional<Produit> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getCategorie()).isEqualTo("Électronique");
    }

    @Test
    @DisplayName("Devrait trouver par catégorie")
    void shouldFindByCategorie() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
        createAndSaveProduit("iPhone", "Électronique", new BigDecimal("8000"), new BigDecimal("0.2"));
        createAndSaveProduit("T-Shirt", "Vêtements", new BigDecimal("200"), new BigDecimal("0.3"));

        Page<Produit> result = repository.findByCategorie("Électronique", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .allMatch(p -> p.getCategorie().equals("Électronique"));
    }

    @Test
    @DisplayName("Devrait retourner liste vide pour catégorie inexistante")
    void shouldReturnEmptyForNonExistentCategorie() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

        Page<Produit> result = repository.findByCategorie("NonExistant", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Devrait rechercher par nom de produit")
    void shouldSearchByNom() {
        createAndSaveProduit("Laptop Dell XPS", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
        createAndSaveProduit("iPhone 13", "Électronique", new BigDecimal("8000"), new BigDecimal("0.2"));

        Page<Produit> result = repository.searchByKeyword("Dell", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).contains("Dell");
    }

    @Test
    @DisplayName("Devrait rechercher par catégorie")
    void shouldSearchByCategorie() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
        createAndSaveProduit("T-Shirt", "Vêtements", new BigDecimal("200"), new BigDecimal("0.3"));

        Page<Produit> result = repository.searchByKeyword("Vêtements", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCategorie()).isEqualTo("Vêtements");
    }

    @Test
    @DisplayName("Devrait rechercher de manière insensible à la casse")
    void shouldSearchCaseInsensitive() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

        Page<Produit> result = repository.searchByKeyword("laptop", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait rechercher avec résultats multiples")
    void shouldSearchWithMultipleResults() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));
        createAndSaveProduit("Laptop HP", "Électronique", new BigDecimal("4500"), new BigDecimal("2.3"));
        createAndSaveProduit("iPhone", "Électronique", new BigDecimal("8000"), new BigDecimal("0.2"));

        Page<Produit> result = repository.searchByKeyword("Laptop", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Devrait retourner liste vide si aucun résultat")
    void shouldReturnEmptyWhenNoMatch() {
        createAndSaveProduit("Laptop Dell", "Électronique", new BigDecimal("5000"), new BigDecimal("2.5"));

        Page<Produit> result = repository.searchByKeyword("NonExistant", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    private Produit createProduit(String nom, String categorie, BigDecimal prixUnitaire, BigDecimal poids) {
        Produit produit = new Produit();
        produit.setNom(nom);
        produit.setCategorie(categorie);
        produit.setPrix(prixUnitaire);
        produit.setPoids(poids);
        return produit;
    }

    private void createAndSaveProduit(String nom, String categorie, BigDecimal prixUnitaire, BigDecimal poids) {
        repository.save(createProduit(nom, categorie, prixUnitaire, poids));
    }
}