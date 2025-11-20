package com.toctoc.colis.domain.repository;

import com.toctoc.colis.domain.model.ColisProduit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ColisProduitRepository extends JpaRepository<ColisProduit, String> {

    List<ColisProduit> findByColisId(String colisId);

    void deleteByColisId(String colisId);
}