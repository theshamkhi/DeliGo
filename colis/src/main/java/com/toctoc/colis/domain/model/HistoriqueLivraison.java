package com.toctoc.colis.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "historique_livraison")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoriqueLivraison {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", length = 36)
    private String id;

    @NotNull(message = "Le colis est obligatoire")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "colis_id", nullable = false)
    private Colis colis;

    @NotNull(message = "Le statut est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false, length = 20)
    private StatutColis statut;

    @NotNull(message = "La date de changement est obligatoire")
    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Size(max = 500)
    @Column(name = "commentaire", length = 500)
    private String commentaire;

    @Size(max = 100)
    @Column(name = "modifie_par", length = 100)
    private String modifiePar;

    @PrePersist
    protected void onCreate() {
        if (dateChangement == null) {
            dateChangement = LocalDateTime.now();
        }
    }
}