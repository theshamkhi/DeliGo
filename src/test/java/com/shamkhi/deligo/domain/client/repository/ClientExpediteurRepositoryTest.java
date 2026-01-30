package com.shamkhi.deligo.domain.client.repository;

import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du ClientExpediteurRepository")
class ClientExpediteurRepositoryTest {

    @Autowired
    private ClientExpediteurRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer")
    void shouldSaveAndFind() {
        ClientExpediteur client = new ClientExpediteur();
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setEmail("jean@test.com");
        client.setTelephone("0612345678");
        client.setAdresse("123 Rue Test");

        ClientExpediteur saved = repository.save(client);

        assertThat(saved.getId()).isNotNull();

        Optional<ClientExpediteur> found = repository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("jean@test.com");
    }

    @Test
    @DisplayName("Devrait trouver par email")
    void shouldFindByEmail() {
        ClientExpediteur client = new ClientExpediteur();
        client.setNom("Test");
        client.setPrenom("User");
        client.setEmail("unique@test.com");
        client.setTelephone("0612345678");
        client.setAdresse("Address");
        repository.save(client);

        Optional<ClientExpediteur> found = repository.findByEmail("unique@test.com");

        assertThat(found).isPresent();
        assertThat(found.get().getNom()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Devrait vérifier si email existe")
    void shouldCheckEmailExists() {
        ClientExpediteur client = new ClientExpediteur();
        client.setNom("Test");
        client.setPrenom("User");
        client.setEmail("exists@test.com");
        client.setTelephone("0612345678");
        client.setAdresse("Address");
        repository.save(client);

        boolean exists = repository.existsByEmail("exists@test.com");
        boolean notExists = repository.existsByEmail("notfound@test.com");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }
}