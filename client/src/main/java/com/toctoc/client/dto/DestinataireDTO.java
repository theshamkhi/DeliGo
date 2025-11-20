package com.toctoc.client.dto;

import jakarta.validation.constraints.Email;
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
public class DestinataireDTO {
    private String id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    private String prenom;

    @Email
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Le téléphone doit être un numéro marocain valide (ex: 0612345678)")
    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20)
    private String telephone;

    @NotBlank(message = "L'adresse est obligatoire")
    @Size(max = 255)
    private String adresse;

    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}