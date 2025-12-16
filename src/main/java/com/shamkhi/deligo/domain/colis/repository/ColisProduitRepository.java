package com.shamkhi.deligo.domain.colis.repository;

import com.shamkhi.deligo.domain.colis.model.ColisProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColisProduitRepository extends JpaRepository<ColisProduit, String> {

    List<ColisProduit> findByColisId(String colisId);

    void deleteByColisId(String colisId);
}