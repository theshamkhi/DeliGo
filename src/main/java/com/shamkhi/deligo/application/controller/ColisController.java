package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.colis.dto.*;
import com.shamkhi.deligo.domain.colis.model.PrioriteColis;
import com.shamkhi.deligo.domain.colis.service.ColisService;
import com.shamkhi.deligo.domain.security.model.User;
import com.shamkhi.deligo.domain.security.repository.UserRepository;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/colis")
@RequiredArgsConstructor
@Tag(name = "Colis", description = "Gestion des colis")
@SecurityRequirement(name = "Bearer Authentication")
public class ColisController {

    private final ColisService service;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Liste tous les colis")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR', 'CLIENT')")
    public ResponseEntity<Page<ColisDTO>> getAll(
            @PageableDefault(size = 20, sort = "dateCreation") Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Si LIVREUR ou CLIENT, filtrer par leurs colis
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getLivreurId() != null) {
                return ResponseEntity.ok(service.getColisByLivreur(user.getLivreurId(), pageable));
            }
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getClientExpediteurId() != null) {
                return ResponseEntity.ok(service.getColisByClient(user.getClientExpediteurId(), pageable));
            }
        }

        // MANAGER voit tout
        return ResponseEntity.ok(service.getAllColis(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupère un colis par ID")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR', 'CLIENT')")
    public ResponseEntity<ColisDTO> getById(@PathVariable String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        ColisDTO colis = service.getColisById(id);

        // Vérifier les droits d'accès
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!colis.getLivreurId().equals(user.getLivreurId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!colis.getClientExpediteurId().equals(user.getClientExpediteurId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.ok(colis);
    }

    @GetMapping("/search")
    @Operation(summary = "Recherche de colis")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR', 'CLIENT')")
    public ResponseEntity<Page<ColisDTO>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Appliquer les mêmes filtres que getAll
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            return ResponseEntity.ok(service.searchColis(keyword, pageable));
        }

        // Pour LIVREUR et CLIENT, on peut ajouter une logique de filtrage
        return ResponseEntity.ok(service.searchColis(keyword, pageable));
    }

    @PostMapping
    @Operation(summary = "Crée un colis")
    @PreAuthorize("hasAnyRole('MANAGER', 'CLIENT')")
    public ResponseEntity<ColisDTO> create(@Valid @RequestBody CreateColisRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Si CLIENT, s'assurer qu'il crée pour lui-même
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            String username = auth.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (!request.getClientExpediteurId().equals(user.getClientExpediteurId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(service.createColis(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Met à jour un colis")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ColisDTO> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateColisRequest request) {
        return ResponseEntity.ok(service.updateColis(id, request));
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Met à jour le statut d'un colis")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR')")
    public ResponseEntity<ColisDTO> updateStatut(
            @PathVariable String id,
            @Valid @RequestBody UpdateStatutRequest request) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        ColisDTO colis = service.getColisById(id);

        // Si LIVREUR, vérifier qu'il est assigné au colis
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getLivreurId() == null || !user.getLivreurId().equals(colis.getLivreurId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(null);
            }

            if (request.getModifiePar() == null || request.getModifiePar().isEmpty()) {
                request.setModifiePar(user.getUsername());
            }
        }

        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_MANAGER"))) {
            if (request.getModifiePar() == null || request.getModifiePar().isEmpty()) {
                request.setModifiePar(username);
            }
        }

        return ResponseEntity.ok(service.updateStatut(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprime un colis")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.deleteColis(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistiques")
    @Operation(summary = "Récupère les statistiques détaillées des colis par statut")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR', 'CLIENT')")
    public ResponseEntity<ColisStatisticsResponse> getStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        ColisStatisticsResponse stats = service.getStatisticsByUser(username, roles);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/statistiques/livreur/{livreurId}")
    @Operation(summary = "Statistiques d'un livreur spécifique")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ColisStatisticsResponse> getStatisticsByLivreur(
            @PathVariable String livreurId) {
        return ResponseEntity.ok(service.getStatisticsByLivreurId(livreurId));
    }

    @GetMapping("/en-retard")
    @Operation(summary = "Liste des colis en retard")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR')")
    public ResponseEntity<List<ColisDTO>> getOverdueColis() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Si LIVREUR, filtrer par ses colis uniquement
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getLivreurId() != null) {
                List<ColisDTO> allOverdue = service.getOverdueColis();
                // Filtrer seulement les colis du livreur
                return ResponseEntity.ok(
                        allOverdue.stream()
                                .filter(c -> user.getLivreurId().equals(c.getLivreurId()))
                                .collect(Collectors.toList())
                );
            }
        }

        return ResponseEntity.ok(service.getOverdueColis());
    }

    @GetMapping("/priorite/{priorite}")
    @Operation(summary = "Liste des colis par priorité")
    @PreAuthorize("hasAnyRole('MANAGER', 'LIVREUR')")
    public ResponseEntity<Page<ColisDTO>> getColisByPriorite(
            @PathVariable PrioriteColis priorite,
            @PageableDefault(size = 20) Pageable pageable) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        // Appliquer les mêmes filtres de rôle que getAll()
        if (auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_LIVREUR"))) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

            if (user.getLivreurId() != null) {
                return ResponseEntity.ok(
                        service.getColisByMultipleCriteria(null, priorite, null, null,
                                user.getLivreurId(), pageable)
                );
            }
        }

        return ResponseEntity.ok(
                service.getColisByMultipleCriteria(null, priorite, null, null, null, pageable)
        );
    }
}