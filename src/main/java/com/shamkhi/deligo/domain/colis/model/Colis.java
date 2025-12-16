package com.shamkhi.deligo.domain.colis.model;

import com.shamkhi.deligo.domain.livraison.model.Livreur;
import com.shamkhi.deligo.domain.livraison.model.Zone;
import com.shamkhi.deligo.domain.client.model.Destinataire;
import com.shamkhi.deligo.domain.client.model.ClientExpediteur;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "colis")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Colis {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 500)
    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @NotNull(message = "Le poids est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le poids doit être supérieur à 0")
    @Column(name = "poids", nullable = false, precision = 10, scale = 2)
    private BigDecimal poids;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutColis statut = StatutColis.CREE;

    @NotNull(message = "La priorité est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "priorite", nullable = false, length = 20)
    private PrioriteColis priorite = PrioriteColis.NORMALE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "livreur_id")
    private Livreur livreur;

    @NotNull(message = "Le client expéditeur est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_expediteur_id", nullable = false)
    private ClientExpediteur clientExpediteur;

    @NotNull(message = "Le destinataire est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destinataire_id", nullable = false)
    private Destinataire destinataire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @NotBlank(message = "La ville de destination est obligatoire")
    @Size(max = 100)
    @Column(name = "ville_destination", nullable = false, length = 100)
    private String villeDestination;

    @Column(name = "date_limite_livraison")
    private LocalDateTime dateLimiteLivraison;

    @Column(name = "date_collecte")
    private LocalDateTime dateCollecte;

    @Column(name = "date_livraison")
    private LocalDateTime dateLivraison;

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