package com.toctoc.toctoc2.domain.colis.dto;

import com.toctoc.toctoc2.domain.colis.model.PrioriteColis;
import com.toctoc.toctoc2.domain.colis.model.StatutColis;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateColisRequest {

    @Size(max = 500)
    private String description;

    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal poids;

    private StatutColis statut;

    private PrioriteColis priorite;

    @Size(max = 100)
    private String villeDestination;

    private LocalDateTime dateLimiteLivraison;

    private String livreurId;

    private String zoneId;
}