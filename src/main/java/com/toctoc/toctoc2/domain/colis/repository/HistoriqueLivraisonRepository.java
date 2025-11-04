package com.toctoc.toctoc2.domain.colis.repository;

import com.toctoc.toctoc2.domain.colis.model.HistoriqueLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueLivraisonRepository extends JpaRepository<HistoriqueLivraison, String> {

    List<HistoriqueLivraison> findByColisIdOrderByDateChangementDesc(String colisId);
}