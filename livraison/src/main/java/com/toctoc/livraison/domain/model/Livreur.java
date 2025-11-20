package com.toctoc.livraison.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "livreur")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livreur {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100)
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100)
    @Column(name = "prenom", nullable = false, length = 100)
    private String prenom;

    @Pattern(regexp = "^(\\+212|0)[5-7][0-9]{8}$", message = "Le téléphone doit être un numéro marocain valide (ex: 0612345678)")
    @NotBlank(message = "Le téléphone est obligatoire")
    @Size(max = 20)
    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;

    @Size(max = 100)
    @Column(name = "vehicule", length = 100)
    private String vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_assignee_id")
    private Zone zoneAssignee;

    @Column(name = "actif")
    private Boolean actif = true;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}