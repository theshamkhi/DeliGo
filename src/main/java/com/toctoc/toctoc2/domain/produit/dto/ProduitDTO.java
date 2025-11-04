package com.toctoc.toctoc2.domain.produit.dto;

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
public class ProduitDTO {
    private String id;

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 150)
    private String nom;

    @Size(max = 100)
    private String categorie;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal poids;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal prix;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}