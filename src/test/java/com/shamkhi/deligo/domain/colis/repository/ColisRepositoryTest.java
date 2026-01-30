package com.shamkhi.deligo.domain.colis.repository;

import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import com.shamkhi.deligo.domain.client.model.Destinataire;
import com.shamkhi.deligo.domain.client.repository.ClientExpediteurRepository;
import com.shamkhi.deligo.domain.client.repository.DestinataireRepository;
import com.shamkhi.deligo.domain.colis.model.Colis;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.model.StatutColis;
import com.shamkhi.deligo.domain.colis.model.*;
import com.shamkhi.deligo.domain.client.model.*;
import com.shamkhi.deligo.domain.client.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Tests du ColisRepository")
class ColisRepositoryTest {

    @Autowired private ColisRepository colisRepository;
    @Autowired private ClientExpediteurRepository clientRepository;
    @Autowired private DestinataireRepository destinataireRepository;

    private ClientExpediteur client;
    private Destinataire destinataire;

    @BeforeEach
    void setUp() {
        colisRepository.deleteAll();

        client = new ClientExpediteur();
        client.setNom("Test");
        client.setPrenom("User");
        client.setEmail("test@test.com");
        client.setTelephone("0612345678");
        client.setAdresse("Address");
        client = clientRepository.save(client);

        destinataire = new Destinataire();
        destinataire.setNom("Dest");
        destinataire.setPrenom("User");
        destinataire.setTelephone("0698765432");
        destinataire.setAdresse("Address");
        destinataire = destinataireRepository.save(destinataire);
    }

    @Test
    @DisplayName("Devrait sauvegarder et récupérer un colis")
    void shouldSaveAndRetrieve() {
        Colis colis = createColis("Laptop", StatutColis.CREE);
        Colis saved = colisRepository.save(colis);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isEqualTo("Laptop");

        Optional<Colis> found = colisRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Laptop");
    }

    @Test
    @DisplayName("Devrait trouver par statut")
    void shouldFindByStatut() {
        createAndSaveColis("Colis 1", StatutColis.CREE);
        createAndSaveColis("Colis 2", StatutColis.CREE);
        createAndSaveColis("Colis 3", StatutColis.EN_TRANSIT);

        Page<Colis> result = colisRepository.findByStatut(
                StatutColis.CREE,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("Devrait trouver par priorité")
    void shouldFindByPriorite() {
        Colis urgent = createColis("Urgent", StatutColis.CREE);
        urgent.setPriorite(PrioriteColis.URGENT);
        colisRepository.save(urgent);

        Colis normal = createColis("Normal", StatutColis.CREE);
        normal.setPriorite(PrioriteColis.NORMALE);
        colisRepository.save(normal);

        Page<Colis> result = colisRepository.findByPriorite(
                PrioriteColis.URGENT,
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getPriorite()).isEqualTo(PrioriteColis.URGENT);
    }

    @Test
    @DisplayName("Devrait rechercher par mot-clé")
    void shouldSearchByKeyword() {
        createAndSaveColis("Laptop Dell", StatutColis.CREE);
        createAndSaveColis("Phone Samsung", StatutColis.CREE);

        Page<Colis> result = colisRepository.searchByKeyword(
                "Dell",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getDescription()).contains("Dell");
    }

    @Test
    @DisplayName("Devrait trouver par ville de destination")
    void shouldFindByVilleDestination() {
        Colis casa = createColis("Colis Casa", StatutColis.CREE);
        casa.setVilleDestination("Casablanca");
        colisRepository.save(casa);

        Colis rabat = createColis("Colis Rabat", StatutColis.CREE);
        rabat.setVilleDestination("Rabat");
        colisRepository.save(rabat);

        Page<Colis> result = colisRepository.findByVilleDestinationContainingIgnoreCase(
                "casa",
                PageRequest.of(0, 10)
        );

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Devrait trouver les colis en retard")
    void shouldFindOverdueColis() {
        Colis overdue = createColis("Overdue", StatutColis.EN_TRANSIT);
        overdue.setDateLimiteLivraison(LocalDateTime.now().minusDays(1));
        colisRepository.save(overdue);

        Colis onTime = createColis("OnTime", StatutColis.EN_TRANSIT);
        onTime.setDateLimiteLivraison(LocalDateTime.now().plusDays(1));
        colisRepository.save(onTime);

        List<StatutColis> excludedStatuses = Arrays.asList(
                StatutColis.LIVRE,
                StatutColis.ANNULE
        );
        List<Colis> result = colisRepository.findOverdueColis(
                LocalDateTime.now(),
                excludedStatuses
        );

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Devrait compter par statut")
    void shouldCountByStatut() {
        createAndSaveColis("C1", StatutColis.CREE);
        createAndSaveColis("C2", StatutColis.CREE);
        createAndSaveColis("C3", StatutColis.EN_TRANSIT);

        List<Object[]> stats = colisRepository.countByStatut();

        assertThat(stats).isNotEmpty();
        assertThat(stats.size()).isGreaterThanOrEqualTo(2);
    }

    private Colis createColis(String description, StatutColis statut) {
        Colis colis = new Colis();
        colis.setDescription(description);
        colis.setPoids(BigDecimal.valueOf(2.5));
        colis.setStatut(statut);
        colis.setPriorite(PrioriteColis.NORMALE);
        colis.setVilleDestination("Casablanca");
        colis.setClientExpediteur(client);
        colis.setDestinataire(destinataire);
        return colis;
    }

    private void createAndSaveColis(String description, StatutColis statut) {
        colisRepository.save(createColis(description, statut));
    }
}