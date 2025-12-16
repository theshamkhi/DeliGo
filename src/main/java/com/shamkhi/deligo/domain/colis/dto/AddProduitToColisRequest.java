package com.shamkhi.deligo.domain.colis.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddProduitToColisRequest {

    @NotNull(message = "Le produit est obligatoire")
    private String produitId;

    @NotNull(message = "La quantité est obligatoire")
    @Min(value = 1)
    private Integer quantite;

    @NotNull(message = "Le prix est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal prix;
}