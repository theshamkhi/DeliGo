package com.toctoc.toctoc2.domain.colis.repository;

import com.toctoc.toctoc2.domain.colis.model.ColisProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColisProduitRepository extends JpaRepository<ColisProduit, String> {

    List<ColisProduit> findByColisId(String colisId);

    void deleteByColisId(String colisId);
}