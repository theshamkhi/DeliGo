package com.toctoc.client.controller;

import com.toctoc.client.domain.service.ClientExpediteurService;
import com.toctoc.client.dto.ClientExpediteurDTO;
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
@RequestMapping("/clients")
@RequiredArgsConstructor
@Tag(name = "Clients Expéditeurs", description = "Gestion des clients expéditeurs")
public class ClientExpediteurController {

    private final ClientExpediteurService service;

    @GetMapping
    @Operation(summary = "Liste tous les clients")
    public ResponseEntity<Page<ClientExpediteurDTO>> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.getAllClients(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un client par ID")
    public ResponseEntity<ClientExpediteurDTO> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.getClientById(id));
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de clients")
    public ResponseEntity<Page<ClientExpediteurDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(service.searchClients(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un client")
    public ResponseEntity<ClientExpediteurDTO> create(@Valid @RequestBody ClientExpediteurDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createClient(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un client")
    public ResponseEntity<ClientExpediteurDTO> update(
            @PathVariable String id,
            @Valid @RequestBody ClientExpediteurDTO dto) {
        return ResponseEntity.ok(service.updateClient(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un client")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteClient(id);
        return ResponseEntity.noContent().build();
    }
}