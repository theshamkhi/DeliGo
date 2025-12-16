package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.model.StatutColis;
import com.shamkhi.deligo.domain.colis.service.ColisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colis")
@RequiredArgsConstructor
@Tag(name = "Colis", description = "Gestion des colis")
public class ColisController {

    private final ColisService colisService;

    @GetMapping
    @Operation(summary = "Liste tous les colis avec pagination")
    public ResponseEntity<Page<ColisDTO>> getAllColis(
            @PageableDefault(size = 20, sort = "dateCreation") Pageable pageable) {
        return ResponseEntity.ok(colisService.getAllColis(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un colis par son ID")
    public ResponseEntity<ColisDTO> getColisById(@PathVariable String id) {
        return ResponseEntity.ok(colisService.getColisById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche des colis par mot-clé")
    public ResponseEntity<Page<ColisDTO>> searchColis(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(colisService.searchColis(keyword, pageable));
    }

    @GetMapping("/filter")
    @Operation(summary = "Filtre les colis selon plusieurs critères")
    public ResponseEntity<Page<ColisDTO>> filterColis(
            @RequestParam(required = false) StatutColis statut,
            @RequestParam(required = false) PrioriteColis priorite,
            @RequestParam(required = false) String zoneId,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String livreurId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(
                colisService.getColisByMultipleCriteria(statut, priorite, zoneId, ville, livreurId, pageable));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Liste les colis d'un client expéditeur")
    public ResponseEntity<Page<ColisDTO>> getColisByClient(
            @PathVariable String clientId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(colisService.getColisByClientExpediteur(clientId, pageable));
    }

    @GetMapping("/destinataire/{destinataireId}")
    @Operation(summary = "Liste les colis d'un destinataire")
    public ResponseEntity<Page<ColisDTO>> getColisByDestinataire(
            @PathVariable String destinataireId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(colisService.getColisByDestinataire(destinataireId, pageable));
    }

    @GetMapping("/livreur/{livreurId}")
    @Operation(summary = "Liste les colis assignés à un livreur")
    public ResponseEntity<Page<ColisDTO>> getColisByLivreur(
            @PathVariable String livreurId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(colisService.getColisByLivreur(livreurId, pageable));
    }

    @GetMapping("/overdue")
    @Operation(summary = "Liste les colis en retard")
    public ResponseEntity<List<ColisDTO>> getOverdueColis() {
        return ResponseEntity.ok(colisService.getOverdueColis());
    }

    @PostMapping
    @Operation(summary = "Crée un nouveau colis")
    public ResponseEntity<ColisDTO> createColis(@Valid @RequestBody CreateColisRequest request) {
        ColisDTO created = colisService.createColis(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un colis")
    public ResponseEntity<ColisDTO> updateColis(
            @PathVariable String id,
            @Valid @RequestBody UpdateColisRequest request) {
        return ResponseEntity.ok(colisService.updateColis(id, request));
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Met à jour le statut d'un colis")
    public ResponseEntity<Void> updateStatut(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatutRequest request) {
        colisService.updateStatut(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un colis")
    public ResponseEntity<Void> deleteColis(@PathVariable String id) {
        colisService.deleteColis(id);
        return ResponseEntity.noContent().build();
    }

    // Historique
    @GetMapping("/{id}/historique")
    @Operation(summary = "Récupère l'historique d'un colis")
    public ResponseEntity<List<HistoriqueLivraisonDTO>> getHistorique(@PathVariable String id) {
        return ResponseEntity.ok(colisService.getHistoriqueByColis(id));
    }

    // Produits
    @GetMapping("/{id}/produits")
    @Operation(summary = "Liste les produits d'un colis")
    public ResponseEntity<List<ColisProduitDTO>> getProduits(@PathVariable String id) {
        return ResponseEntity.ok(colisService.getProduitsByColis(id));
    }

    @PostMapping("/{id}/produits")
    @Operation(summary = "Ajoute un produit à un colis")
    public ResponseEntity<ColisProduitDTO> addProduit(
            @PathVariable String id,
            @Valid @RequestBody AddProduitToColisRequest request) {
        ColisProduitDTO added = colisService.addProduitToColis(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @DeleteMapping("/produits/{colisProduitId}")
    @Operation(summary = "Retire un produit d'un colis")
    public ResponseEntity<Void> removeProduit(@PathVariable String colisProduitId) {
        colisService.removeProduitFromColis(colisProduitId);
        return ResponseEntity.noContent().build();
    }

    // Statistiques
    @GetMapping("/statistics/livreur")
    @Operation(summary = "Statistiques par livreur")
    public ResponseEntity<List<ColisStatisticsDTO>> getStatsByLivreur() {
        return ResponseEntity.ok(colisService.getStatisticsByLivreur());
    }

    @GetMapping("/statistics/zone")
    @Operation(summary = "Statistiques par zone")
    public ResponseEntity<List<ColisStatisticsDTO>> getStatsByZone() {
        return ResponseEntity.ok(colisService.getStatisticsByZone());
    }
}