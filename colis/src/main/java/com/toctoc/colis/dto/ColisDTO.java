package com.toctoc.colis.dto;

import com.toctoc.colis.domain.model.PrioriteColis;
import com.toctoc.colis.domain.model.StatutColis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColisDTO {
    private String id;
    private String description;
    private BigDecimal poids;
    private StatutColis statut;
    private PrioriteColis priorite;
    private String villeDestination;
    private LocalDateTime dateLimiteLivraison;
    private LocalDateTime dateCollecte;
    private LocalDateTime dateLivraison;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;

    // Relations simplifiées
    private String livreurId;
    private String livreurNom;
    private String clientExpediteurId;
    private String clientExpediteurNom;
    private String destinataireId;
    private String destinataireNom;
    private String zoneId;
    private String zoneNom;
}