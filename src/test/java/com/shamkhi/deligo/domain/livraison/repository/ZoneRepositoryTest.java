package com.shamkhi.deligo.domain.livraison.repository;

import com.shamkhi.deligo.domain.livraison.model.Zone;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du ZoneRepository")
class ZoneRepositoryTest {

    @Autowired private ZoneRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer une zone")
    void shouldSaveAndRetrieve() {
        Zone zone = createZone("Centre", "20000", "Casablanca");
        Zone saved = repository.save(zone);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo("Centre");
    }

    @Test
    @DisplayName("Devrait trouver par code postal")
    void shouldFindByCodePostal() {
        createAndSaveZone("Centre", "20000", "Casablanca");
        createAndSaveZone("Nord", "20100", "Casablanca");

        Optional<Zone> found = repository.findByCodePostal("20000");

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Centre");
    }

    @Test
    @DisplayName("Devrait retourner vide si code postal inexistant")
    void shouldReturnEmptyWhenCodePostalNotFound() {
        Optional<Zone> found = repository.findByCodePostal("99999");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Devrait trouver par ville")
    void shouldFindByVille() {
        createAndSaveZone("Centre", "20000", "Casablanca");
        createAndSaveZone("Agdal", "10000", "Rabat");
        createAndSaveZone("Maarif", "20200", "Casablanca");

        Page<Zone> result = repository.findByVilleContainingIgnoreCase(
                "Casablanca",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Devrait chercher ville de manière insensible à la casse")
    void shouldFindByVilleCaseInsensitive() {
        createAndSaveZone("Centre", "20000", "Casablanca");

        Page<Zone> result = repository.findByVilleContainingIgnoreCase(
                "casablanca",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur nom")
    void shouldSearchByNom() {
        createAndSaveZone("Centre-Ville", "20000", "Casablanca");
        createAndSaveZone("Agdal", "10000", "Rabat");

        Page<Zone> result = repository.searchByKeyword("Centre", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).contains("Centre");
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur code postal")
    void shouldSearchByCodePostal() {
        createAndSaveZone("Centre", "20000", "Casablanca");
        createAndSaveZone("Nord", "20100", "Casablanca");

        Page<Zone> result = repository.searchByKeyword("20000", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur ville")
    void shouldSearchByVille() {
        createAndSaveZone("Centre", "20000", "Casablanca");
        createAndSaveZone("Agdal", "10000", "Rabat");

        Page<Zone> result = repository.searchByKeyword("Rabat", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait rechercher de manière insensible à la casse")
    void shouldSearchCaseInsensitive() {
        createAndSaveZone("Centre-Ville", "20000", "Casablanca");

        Page<Zone> result = repository.searchByKeyword("centre", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait retourner liste vide si aucun résultat")
    void shouldReturnEmptyWhenNoMatch() {
        createAndSaveZone("Centre", "20000", "Casablanca");

        Page<Zone> result = repository.searchByKeyword("NonExistant", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    private Zone createZone(String nom, String codePostal, String ville) {
        Zone zone = new Zone();
        zone.setNom(nom);
        zone.setCodePostal(codePostal);
        zone.setVille(ville);
        return zone;
    }

    private void createAndSaveZone(String nom, String codePostal, String ville) {
        repository.save(createZone(nom, codePostal, ville));
    }
}