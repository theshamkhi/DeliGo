package com.shamkhi.deligo.domain.livraison.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    // User credentials - required for creation
    @NotBlank(message = "Le nom d'utilisateur est obligatoire", groups = CreateValidation.class)
    @Size(min = 3, max = 50, groups = CreateValidation.class)
    private String username;

    @NotBlank(message = "L'email est obligatoire", groups = CreateValidation.class)
    @Email(message = "L'email doit être valide", groups = CreateValidation.class)
    @Size(max = 150, groups = CreateValidation.class)
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire", groups = CreateValidation.class)
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères", groups = CreateValidation.class)
    private String password;

    // Read-only - returned after creation
    private String userId;

    // Validation groups
    public interface CreateValidation {}
    public interface UpdateValidation {}
}