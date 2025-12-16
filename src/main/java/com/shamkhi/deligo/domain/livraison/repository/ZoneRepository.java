package com.shamkhi.deligo.domain.livraison.repository;

import com.shamkhi.deligo.domain.livraison.model.Zone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneRepository extends JpaRepository<Zone, String> {

    Optional<Zone> findByCodePostal(String codePostal);

    Page<Zone> findByVilleContainingIgnoreCase(String ville, Pageable pageable);

    @Query("SELECT z FROM Zone z WHERE " +
            "LOWER(z.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(z.codePostal) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(z.ville) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Zone> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}