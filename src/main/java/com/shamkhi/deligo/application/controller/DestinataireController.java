package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.client.dto.DestinataireDTO;
import com.shamkhi.deligo.domain.client.service.DestinataireService;
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
@RequestMapping("/destinataires")
@RequiredArgsConstructor
@Tag(name = "Destinataires", description = "Gestion des destinataires")
public class DestinataireController {

    private final DestinataireService service;

    @GetMapping
    @Operation(summary = "Liste tous les destinataires")
    public ResponseEntity<Page<DestinataireDTO>> getAll(
            @PageableDefault(size = 20, sort = "nom") Pageable pageable) {
        return ResponseEntity.ok(service.getAllDestinataires(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un destinataire par ID")
    public ResponseEntity<DestinataireDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getDestinataireById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de destinataires")
    public ResponseEntity<Page<DestinataireDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchDestinataires(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un destinataire")
    public ResponseEntity<DestinataireDTO> create(@Valid @RequestBody DestinataireDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createDestinataire(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un destinataire")
    public ResponseEntity<DestinataireDTO> update(
            @PathVariable String id,
            @Valid @RequestBody DestinataireDTO dto) {
        return ResponseEntity.ok(service.updateDestinataire(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un destinataire")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteDestinataire(id);
        return ResponseEntity.noContent().build();
    }
}