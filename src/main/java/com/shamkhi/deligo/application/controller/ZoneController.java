package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.livraison.dto.ZoneDTO;
import com.shamkhi.deligo.domain.livraison.service.ZoneService;
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
@RequestMapping("/zones")
@RequiredArgsConstructor
@Tag(name = "Zones", description = "Gestion des zones géographiques")
public class ZoneController {

    private final ZoneService service;

    @GetMapping
    @Operation(summary = "Liste toutes les zones")
    public ResponseEntity<Page<ZoneDTO>> getAll(
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(service.getAllZones(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère une zone par ID")
    public ResponseEntity<ZoneDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getZoneById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de zones")
    public ResponseEntity<Page<ZoneDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchZones(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée une zone")
    public ResponseEntity<ZoneDTO> create(@Valid @RequestBody ZoneDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createZone(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour une zone")
    public ResponseEntity<ZoneDTO> update(
            @PathVariable String id,
            @Valid @RequestBody ZoneDTO dto) {
        return ResponseEntity.ok(service.updateZone(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime une zone")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteZone(id);
        return ResponseEntity.noContent().build();
    }
}