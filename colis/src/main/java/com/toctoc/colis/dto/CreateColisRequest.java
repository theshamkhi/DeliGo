package com.toctoc.colis.dto;

import com.toctoc.colis.domain.model.PrioriteColis;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateColisRequest {

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500)
    private String description;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal poids;

    @NotNull(message = "La priorité est obligatoire")
    private PrioriteColis priorite;

    @NotBlank(message = "La ville de destination est obligatoire")
    @Size(max = 100)
    private String villeDestination;

    private LocalDateTime dateLimiteLivraison;

    @NotNull(message = "Le client expéditeur est obligatoire")
    private String clientExpediteurId;

    @NotNull(message = "Le destinataire est obligatoire")
    private String destinataireId;

    private String zoneId;
}