package com.toctoc.livraison.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LivreurDTO {
    private String id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Le téléphone doit être un numéro marocain valide (ex: 0612345678)")
    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20)
    private String telephone;

    @Size(max = 100)
    private String vehicule;

    private String zoneAssigneeId;
    private String zoneAssigneeNom;

    private Boolean actif;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}