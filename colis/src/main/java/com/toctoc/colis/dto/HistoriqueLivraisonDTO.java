package com.toctoc.colis.dto;

import com.toctoc.colis.domain.model.StatutColis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueLivraisonDTO {
    private String id;
    private StatutColis statut;
    private LocalDateTime dateChangement;
    private String commentaire;
    private String modifiePar;
}