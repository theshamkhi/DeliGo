package com.toctoc.toctoc2.domain.produit.repository;

import com.toctoc.toctoc2.domain.produit.model.Produit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, String> {

    Page<Produit> findByCategorie(String categorie, Pageable pageable);

    @Query("SELECT p FROM Produit p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.categorie) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Produit> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}