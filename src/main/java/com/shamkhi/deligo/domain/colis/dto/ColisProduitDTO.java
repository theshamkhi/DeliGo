package com.shamkhi.deligo.domain.colis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColisProduitDTO {
    private String id;
    private String produitId;
    private String produitNom;
    private Integer quantite;
    private BigDecimal prix;
    private LocalDateTime dateAjout;
}