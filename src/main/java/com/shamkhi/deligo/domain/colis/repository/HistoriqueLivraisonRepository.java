package com.shamkhi.deligo.domain.colis.repository;

import com.shamkhi.deligo.domain.colis.model.HistoriqueLivraison;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoriqueLivraisonRepository extends JpaRepository<HistoriqueLivraison, String> {

    List<HistoriqueLivraison> findByColisIdOrderByDateChangementDesc(String colisId);
}