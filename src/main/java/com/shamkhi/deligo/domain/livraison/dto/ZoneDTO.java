package com.shamkhi.deligo.domain.livraison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDTO {
    private String id;

    @NotBlank(message = "Le nom de la zone est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le code postal est obligatoire")
    @Size(max = 10)
    private String codePostal;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    private String ville;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}