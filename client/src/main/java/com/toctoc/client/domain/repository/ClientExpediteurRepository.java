package com.toctoc.client.domain.repository;

import com.toctoc.client.domain.model.ClientExpediteur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientExpediteurRepository extends JpaRepository<ClientExpediteur, String> {

    Optional<ClientExpediteur> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT c FROM ClientExpediteur c WHERE " +
            "LOWER(c.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.telephone) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ClientExpediteur> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}