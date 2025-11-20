package com.toctoc.colis.domain.repository;

import com.toctoc.colis.domain.model.Colis;
import com.toctoc.colis.domain.model.PrioriteColis;
import com.toctoc.colis.domain.model.StatutColis;
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

    // Recherche par livreur
    Page<Colis> findByLivreurId(String livreurId, Pageable pageable);

    // Recherche par client expéditeur
    Page<Colis> findByClientExpediteurId(String clientId, Pageable pageable);

    // Recherche par destinataire
    Page<Colis> findByDestinataireId(String destinataireId, Pageable pageable);

    // Recherche multi-critères
    @Query("SELECT c FROM Colis c WHERE " +
            "(:statut IS NULL OR c.statut = :statut) AND " +
            "(:priorite IS NULL OR c.priorite = :priorite) AND " +
            "(:zoneId IS NULL OR c.zone.id = :zoneId) AND " +
            "(:ville IS NULL OR LOWER(c.villeDestination) LIKE LOWER(CONCAT('%', :ville, '%'))) AND " +
            "(:livreurId IS NULL OR c.livreur.id = :livreurId)")
    Page<Colis> findByMultipleCriteria(
            @Param("statut") StatutColis statut,
            @Param("priorite") PrioriteColis priorite,
            @Param("zoneId") String zoneId,
            @Param("ville") String ville,
            @Param("livreurId") String livreurId,
            Pageable pageable
    );

    // Recherche globale par mot-clé
    @Query("SELECT c FROM Colis c WHERE " +
            "LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.villeDestination) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.clientExpediteur.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.destinataire.nom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Colis> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // Colis en retard
    @Query("SELECT c FROM Colis c WHERE c.dateLimiteLivraison < :now AND c.statut NOT IN :excludedStatuses")
    List<Colis> findOverdueColis(
            @Param("now") LocalDateTime now,
            @Param("excludedStatuses") List<StatutColis> excludedStatuses
    );

    // Statistiques par livreur
    @Query("SELECT c.livreur.id, COUNT(c), SUM(c.poids) FROM Colis c " +
            "WHERE c.livreur IS NOT NULL " +
            "GROUP BY c.livreur.id")
    List<Object[]> countAndSumWeightByLivreur();

    // Statistiques par zone
    @Query("SELECT c.zone.id, c.zone.nom, COUNT(c), SUM(c.poids) FROM Colis c " +
            "WHERE c.zone IS NOT NULL " +
            "GROUP BY c.zone.id, c.zone.nom")
    List<Object[]> countAndSumWeightByZone();

    // Statistiques par statut
    @Query("SELECT c.statut, COUNT(c) FROM Colis c GROUP BY c.statut")
    List<Object[]> countByStatut();

    // Statistiques par priorité
    @Query("SELECT c.priorite, COUNT(c) FROM Colis c GROUP BY c.priorite")
    List<Object[]> countByPriorite();

    // Colis d'un livreur non livrés
    @Query("SELECT c FROM Colis c WHERE c.livreur.id = :livreurId AND c.statut != :statut")
    List<Colis> findByLivreurIdAndStatutNot(
            @Param("livreurId") String livreurId,
            @Param("statut") StatutColis statut
    );
}