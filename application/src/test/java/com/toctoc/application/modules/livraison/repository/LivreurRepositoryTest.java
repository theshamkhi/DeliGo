package com.toctoc.application.modules.livraison.repository;

import com.toctoc.livraison.domain.model.Livreur;
import com.toctoc.livraison.domain.model.Zone;
import com.toctoc.livraison.domain.repository.LivreurRepository;
import com.toctoc.livraison.domain.repository.ZoneRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du LivreurRepository")
class LivreurRepositoryTest {

    @Autowired private LivreurRepository repository;
    @Autowired private ZoneRepository zoneRepository;

    private Zone zone;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        zoneRepository.deleteAll();

        zone = new Zone();
        zone.setNom("Zone Centre");
        zone.setCodePostal("20000");
        zone.setVille("Casablanca");
        zone = zoneRepository.save(zone);
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un livreur")
    void shouldSaveAndRetrieve() {
        Livreur livreur = createLivreur("Alami", "Ahmed", "0612345678");
        Livreur saved = repository.save(livreur);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo("Alami");
        assertThat(saved.getTelephone()).isEqualTo("0612345678");
    }

    @Test
    @DisplayName("Devrait trouver les livreurs actifs")
    void shouldFindByActif() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);
        createAndSaveLivreur("Bennani", "Karim", "0698765432", true);
        createAndSaveLivreur("Idrissi", "Omar", "0687654321", false);

        List<Livreur> actifs = repository.findByActif(true);

        assertThat(actifs).hasSize(2);
        assertThat(actifs).allMatch(Livreur::getActif);
    }

    @Test
    @DisplayName("Devrait trouver les livreurs inactifs")
    void shouldFindInactifs() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);
        createAndSaveLivreur("Idrissi", "Omar", "0687654321", false);

        List<Livreur> inactifs = repository.findByActif(false);

        assertThat(inactifs).hasSize(1);
        assertThat(inactifs.get(0).getActif()).isFalse();
    }

    @Test
    @DisplayName("Devrait trouver les livreurs par zone")
    void shouldFindByZone() {
        Livreur livreur1 = createLivreur("Alami", "Ahmed", "0612345678");
        livreur1.setZoneAssignee(zone);
        repository.save(livreur1);

        Livreur livreur2 = createLivreur("Bennani", "Karim", "0698765432");
        livreur2.setZoneAssignee(zone);
        repository.save(livreur2);

        Livreur livreur3 = createLivreur("Idrissi", "Omar", "0687654321");
        repository.save(livreur3);

        List<Livreur> livreurs = repository.findByZoneAssigneeId(zone.getId());

        assertThat(livreurs).hasSize(2);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur nom")
    void shouldSearchByNom() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);
        createAndSaveLivreur("Bennani", "Karim", "0698765432", true);

        Page<Livreur> result = repository.searchByKeyword("Alami", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Alami");
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur prénom")
    void shouldSearchByPrenom() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);
        createAndSaveLivreur("Bennani", "Karim", "0698765432", true);

        Page<Livreur> result = repository.searchByKeyword("Karim", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPrenom()).isEqualTo("Karim");
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé sur téléphone")
    void shouldSearchByTelephone() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);
        createAndSaveLivreur("Bennani", "Karim", "0698765432", true);

        Page<Livreur> result = repository.searchByKeyword("0612", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait vérifier si téléphone existe")
    void shouldCheckTelephoneExists() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);

        boolean exists = repository.existsByTelephone("0612345678");
        boolean notExists = repository.existsByTelephone("0699999999");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("Devrait rechercher de manière insensible à la casse")
    void shouldSearchCaseInsensitive() {
        createAndSaveLivreur("Alami", "Ahmed", "0612345678", true);

        Page<Livreur> result = repository.searchByKeyword("alami", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    private Livreur createLivreur(String nom, String prenom, String telephone) {
        Livreur livreur = new Livreur();
        livreur.setNom(nom);
        livreur.setPrenom(prenom);
        livreur.setTelephone(telephone);
        livreur.setActif(true);
        return livreur;
    }

    private void createAndSaveLivreur(String nom, String prenom, String telephone, Boolean actif) {
        Livreur livreur = createLivreur(nom, prenom, telephone);
        livreur.setActif(actif);
        repository.save(livreur);
    }
}