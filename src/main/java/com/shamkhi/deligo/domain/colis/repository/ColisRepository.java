package com.shamkhi.deligo.domain.colis.repository;

import com.shamkhi.deligo.domain.colis.model.Colis;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.model.StatutColis;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ColisRepository extends JpaRepository<Colis, String> {

    // Recherche par statut
    Page<Colis> findByStatut(StatutColis statut, Pageable pageable);

    // Recherche par priorité
    Page<Colis> findByPriorite(PrioriteColis priorite, Pageable pageable);

    // Recherche par ville de destination
    Page<Colis> findByVilleDestinationContainingIgnoreCase(String ville, Pageable pageable);

    // Recherche par zone
    Page<Colis> findByZoneId(String zoneId, Pageable pageable);

    // Statistiques par priorité
    @Query("SELECT c.priorite, COUNT(c) FROM Colis c GROUP BY c.priorite")
    List<Object[]> countByPriorite();

    // Colis d'un livreur non livrés
    @Query("SELECT c FROM Colis c WHERE c.livreur.id = :livreurId AND c.statut != :statut")
    List<Colis> findByLivreurIdAndStatutNot(
            @Param("livreurId") String livreurId,
            @Param("statut") StatutColis statut
    );

    long countByStatut(StatutColis statut);
    @Query("SELECT c.statut, COUNT(c) FROM Colis c GROUP BY c.statut")
    List<Object[]> countByStatut();

    @Query("SELECT COUNT(c) FROM Colis c WHERE c.dateLimiteLivraison < :now AND c.statut NOT IN :excludedStatuses")
    long countOverdue(@Param("now") LocalDateTime now, @Param("excludedStatuses") List<StatutColis> excludedStatuses);

    @Query("SELECT c FROM Colis c WHERE c.clientExpediteur.id = :clientExpediteurId")
    Page<Colis> findByClientExpediteurId(@Param("clientExpediteurId") String clientExpediteurId, Pageable pageable);

    @Query("SELECT c FROM Colis c WHERE c.destinataire.id = :destinataireId")
    Page<Colis> findByDestinataireId(@Param("destinataireId") String destinataireId, Pageable pageable);

    @Query("SELECT c FROM Colis c WHERE c.livreur.id = :livreurId")
    Page<Colis> findByLivreurId(@Param("livreurId") String livreurId, Pageable pageable);

    @Query("SELECT c FROM Colis c WHERE " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.villeDestination) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.clientExpediteur.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.destinataire.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Colis> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT c FROM Colis c WHERE " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:priorite IS NULL OR c.priorite = :priorite) AND " +
            "(:zoneId IS NULL OR c.zone.id = :zoneId) AND " +
            "(:ville IS NULL OR LOWER(c.villeDestination) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
            "(:livreurId IS NULL OR c.livreur.id = :livreurId)")
    Page<Colis> findByMultipleCriteria(
            @Param("statut") StatutColis statut,
            @Param("priorite") com.shamkhi.deligo.domain.colis.model.PrioriteColis priorite,
            @Param("zoneId") String zoneId,
            @Param("ville") String ville,
            @Param("livreurId") String livreurId,
            Pageable pageable
    );

    @Query("SELECT c FROM Colis c WHERE c.dateLimiteLivraison < :now AND c.statut NOT IN :excludedStatuses")
    List<Colis> findOverdueColis(
            @Param("now") LocalDateTime now,
            @Param("excludedStatuses") List<StatutColis> excludedStatuses
    );

    @Query("SELECT c.livreur.id, c.livreur.nom, c.livreur.prenom, COUNT(c), SUM(c.poids) " +
            "FROM Colis c WHERE c.livreur IS NOT NULL " +
            "GROUP BY c.livreur.id, c.livreur.nom, c.livreur.prenom")
    List<Object[]> countAndSumWeightByLivreur();

    @Query("SELECT c.zone.id, c.zone.nom, COUNT(c), SUM(c.poids) " +
            "FROM Colis c WHERE c.zone IS NOT NULL " +
            "GROUP BY c.zone.id, c.zone.nom")
    List<Object[]> countAndSumWeightByZone();
}