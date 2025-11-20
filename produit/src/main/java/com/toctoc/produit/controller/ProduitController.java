package com.toctoc.produit.controller;

import com.toctoc.produit.dto.ProduitDTO;
import com.toctoc.produit.domain.service.ProduitService;
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

@RestController
@RequestMapping("/produits")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion du catalogue de produits")
public class ProduitController {

    private final ProduitService service;

    @GetMapping
    @Operation(summary = "Liste tous les produits")
    public ResponseEntity<Page<ProduitDTO>> getAll(
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(service.getAllProduits(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un produit par ID")
    public ResponseEntity<ProduitDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getProduitById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de produits")
    public ResponseEntity<Page<ProduitDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchProduits(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un produit")
    public ResponseEntity<ProduitDTO> create(@Valid @RequestBody ProduitDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createProduit(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un produit")
    public ResponseEntity<ProduitDTO> update(
            @PathVariable String id,
            @Valid @RequestBody ProduitDTO dto) {
        return ResponseEntity.ok(service.updateProduit(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un produit")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteProduit(id);
        return ResponseEntity.noContent().build();
    }
}