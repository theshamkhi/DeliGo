package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.service.ColisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/colis")
@RequiredArgsConstructor
@Tag(name = "Colis", description = "Gestion des colis")
public class ColisController {

    private final ColisService service;

    @GetMapping
    @Operation(summary = "Liste tous les colis")
    public ResponseEntity<Page<ColisDTO>> getAll(
            Authentication auth,
            @PageableDefault(size = 20, sort = "dateCreation") Pageable pageable) {

        return ResponseEntity.ok(service.getAllColisForUser(auth, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un colis par ID")
    public ResponseEntity<ColisDTO> getById(
            @PathVariable String id,
            Authentication auth) {

        return ResponseEntity.ok(service.getColisByIdForUser(id, auth));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de colis")
    public ResponseEntity<Page<ColisDTO>> search(
            @RequestParam String keyword,
            Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(service.searchColisForUser(keyword, auth, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un colis")
    public ResponseEntity<ColisDTO> create(
            @Valid @RequestBody CreateColisRequest request,
            Authentication auth) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createColisForUser(request, auth));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un colis")
    public ResponseEntity<ColisDTO> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateColisRequest request) {

        return ResponseEntity.ok(service.updateColis(id, request));
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Met à jour le statut d'un colis")
    public ResponseEntity<ColisDTO> updateStatut(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatutRequest request,
            Authentication auth) {

        return ResponseEntity.ok(service.updateStatutForUser(id, request, auth));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un colis")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteColis(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistiques")
    @Operation(summary = "Récupère les statistiques des colis")
    public ResponseEntity<ColisStatisticsResponse> getStatistics(Authentication auth) {
        return ResponseEntity.ok(service.getStatisticsForUser(auth));
    }

    @GetMapping("/statistiques/livreur/{livreurId}")
    @Operation(summary = "Statistiques d'un livreur spécifique")
    public ResponseEntity<ColisStatisticsResponse> getStatisticsByLivreur(
            @PathVariable String livreurId) {

        return ResponseEntity.ok(service.getStatisticsByLivreurId(livreurId));
    }

    @GetMapping("/en-retard")
    @Operation(summary = "Liste des colis en retard")
    public ResponseEntity<List<ColisDTO>> getOverdueColis(Authentication auth) {
        return ResponseEntity.ok(service.getOverdueColisForUser(auth));
    }

    @GetMapping("/priorite/{priorite}")
    @Operation(summary = "Liste des colis par priorité")
    public ResponseEntity<Page<ColisDTO>> getColisByPriorite(
            @PathVariable PrioriteColis priorite,
            Authentication auth,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(service.getColisByPrioriteForUser(priorite, auth, pageable));
    }
}