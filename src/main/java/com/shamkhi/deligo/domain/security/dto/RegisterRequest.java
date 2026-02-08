package com.shamkhi.deligo.domain.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

/**
 * Unified registration request for all user types.
 * Supports creating MANAGER, LIVREUR, and CLIENT users in one endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    private String telephone;

    @NotNull(message = "Au moins un rôle doit être spécifié")
    private Set<String> roles;

    /**
     * Required if roles contains ROLE_LIVREUR
     * Contains business-specific data for delivery drivers
     */
    private LivreurCreationData livreurData;

    /**
     * Required if roles contains ROLE_CLIENT
     * Contains business-specific data for clients
     */
    private ClientCreationData clientData;

    /**
     * Business data for creating a Livreur entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LivreurCreationData {
        @NotBlank(message = "Le véhicule est obligatoire")
        @Size(max = 100)
        private String vehicule;

        private String zoneAssigneeId;

        @Builder.Default
        private Boolean actif = true;
    }

    /**
     * Business data for creating a ClientExpediteur entity
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ClientCreationData {
        @NotBlank(message = "L'adresse est obligatoire")
        @Size(max = 255)
        private String adresse;
    }
}