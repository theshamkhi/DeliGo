package com.toctoc.toctoc2.domain.colis.dto;

import com.toctoc.toctoc2.domain.colis.model.StatutColis;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatutRequest {

    @NotNull(message = "Le statut est obligatoire")
    private StatutColis statut;

    @Size(max = 500)
    private String commentaire;

    @Size(max = 100)
    private String modifiePar;
}