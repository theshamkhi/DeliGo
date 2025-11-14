package com.toctoc.toctoc2.domain.client.repository;

import com.toctoc.toctoc2.domain.client.model.Destinataire;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du DestinataireRepository")
class DestinataireRepositoryTest {

    @Autowired private DestinataireRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un destinataire")
    void shouldSaveAndRetrieve() {
        Destinataire destinataire = createDestinataire("Martin", "Marie", "0698765432");
        Destinataire saved = repository.save(destinataire);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getNom()).isEqualTo("Martin");
        assertThat(saved.getTelephone()).isEqualTo("0698765432");

        Optional<Destinataire> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getPrenom()).isEqualTo("Marie");
    }

    @Test
    @DisplayName("Devrait rechercher par nom")
    void shouldSearchByNom() {
        createAndSaveDestinataire("Martin", "Marie", "0698765432");
        createAndSaveDestinataire("Dupont", "Jean", "0612345678");

        Page<Destinataire> result = repository.searchByKeyword("Martin", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNom()).isEqualTo("Martin");
    }

    @Test
    @DisplayName("Devrait rechercher par prénom")
    void shouldSearchByPrenom() {
        createAndSaveDestinataire("Martin", "Marie", "0698765432");
        createAndSaveDestinataire("Dupont", "Jean", "0612345678");

        Page<Destinataire> result = repository.searchByKeyword("Marie", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPrenom()).isEqualTo("Marie");
    }

    @Test
    @DisplayName("Devrait rechercher par téléphone")
    void shouldSearchByTelephone() {
        createAndSaveDestinataire("Martin", "Marie", "0698765432");
        createAndSaveDestinataire("Dupont", "Jean", "0612345678");

        Page<Destinataire> result = repository.searchByKeyword("0698", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTelephone()).contains("0698");
    }

    @Test
    @DisplayName("Devrait rechercher de manière insensible à la casse")
    void shouldSearchCaseInsensitive() {
        createAndSaveDestinataire("Martin", "Marie", "0698765432");

        Page<Destinataire> result = repository.searchByKeyword("martin", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait retourner liste vide si aucun résultat")
    void shouldReturnEmptyWhenNoMatch() {
        createAndSaveDestinataire("Martin", "Marie", "0698765432");

        Page<Destinataire> result = repository.searchByKeyword("NonExistant", PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("Devrait rechercher avec résultats partiels")
    void shouldSearchWithPartialMatch() {
        createAndSaveDestinataire("Martinez", "Marie", "0698765432");
        createAndSaveDestinataire("Martin", "Pierre", "0687654321");
        createAndSaveDestinataire("Dupont", "Jean", "0612345678");

        Page<Destinataire> result = repository.searchByKeyword("Marti", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
    }

    private Destinataire createDestinataire(String nom, String prenom, String telephone) {
        Destinataire destinataire = new Destinataire();
        destinataire.setNom(nom);
        destinataire.setPrenom(prenom);
        destinataire.setTelephone(telephone);
        destinataire.setAdresse("123 Rue Test");
        return destinataire;
    }

    private void createAndSaveDestinataire(String nom, String prenom, String telephone) {
        repository.save(createDestinataire(nom, prenom, telephone));
    }
}