package com.toctoc.livraison.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "zone")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Zone {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "Le nom de la zone est obligatoire")
    @Size(max = 100)
    @Column(name = "nom", nullable = false, length = 100)
    private String nom;

    @NotBlank(message = "Le code postal est obligatoire")
    @Size(max = 10)
    @Column(name = "code_postal", nullable = false, length = 10)
    private String codePostal;

    @NotBlank(message = "La ville est obligatoire")
    @Size(max = 100)
    @Column(name = "ville", nullable = false, length = 100)
    private String ville;

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