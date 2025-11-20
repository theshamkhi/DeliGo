package com.toctoc.livraison.controller;

import com.toctoc.livraison.domain.service.LivreurService;
import com.toctoc.livraison.dto.LivreurDTO;
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
@RequestMapping("/livreurs")
@RequiredArgsConstructor
@Tag(name = "Livreurs", description = "Gestion des livreurs")
public class LivreurController {

    private final LivreurService service;

    @GetMapping
    @Operation(summary = "Liste tous les livreurs")
    public ResponseEntity<Page<LivreurDTO>> getAll(
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(service.getAllLivreurs(pageable));
    }

    @GetMapping("/actifs")
    @Operation(summary = "Liste les livreurs actifs uniquement")
    public ResponseEntity<List<LivreurDTO>> getActifs() {
        return ResponseEntity.ok(service.getActiveLivreurs());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un livreur par ID")
    public ResponseEntity<LivreurDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getLivreurById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de livreurs")
    public ResponseEntity<Page<LivreurDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchLivreurs(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un livreur")
    public ResponseEntity<LivreurDTO> create(@Valid @RequestBody LivreurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createLivreur(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un livreur")
    public ResponseEntity<LivreurDTO> update(
            @PathVariable String id,
            @Valid @RequestBody LivreurDTO dto) {
        return ResponseEntity.ok(service.updateLivreur(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un livreur")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteLivreur(id);
        return ResponseEntity.noContent().build();
    }
}