package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.security.dto.PermissionDTO;
import com.shamkhi.deligo.domain.security.dto.RegisterRequest;
import com.shamkhi.deligo.domain.security.dto.RoleDTO;
import com.shamkhi.deligo.domain.security.dto.UserDTO;
import com.shamkhi.deligo.domain.security.service.PermissionService;
import com.shamkhi.deligo.domain.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Endpoints d'administration (GESTIONNAIRE uniquement)")
@SecurityRequirement(name = "Bearer Authentication")
@PreAuthorize("hasRole('MANAGER')")
public class AdminController {

    private final UserService userService;
    private final PermissionService permissionService;

    // ========== User Management ==========

    @GetMapping("/users")
    @Operation(summary = "Liste tous les utilisateurs")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Récupère un utilisateur par ID")
    public ResponseEntity<UserDTO> getUserById(@PathVariable String id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/users/role/{roleName}")
    @Operation(summary = "Liste les utilisateurs par rôle")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable String roleName) {
        return ResponseEntity.ok(userService.getUsersByRole(roleName));
    }

    @PostMapping("/users")
    @Operation(summary = "Crée un nouvel utilisateur")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PutMapping("/users/{id}")
    @Operation(summary = "Met à jour un utilisateur")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String id,
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Supprime un utilisateur")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/users/{id}/activate")
    @Operation(summary = "Active un utilisateur")
    public ResponseEntity<Void> activateUser(@PathVariable String id) {
        userService.activateUser(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/users/{id}/deactivate")
    @Operation(summary = "Désactive un utilisateur")
    public ResponseEntity<Void> deactivateUser(@PathVariable String id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    // ========== Permission Management ==========

    @GetMapping("/permissions")
    @Operation(summary = "Liste toutes les permissions")
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/permissions/{id}")
    @Operation(summary = "Récupère une permission par ID")
    public ResponseEntity<PermissionDTO> getPermissionById(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @GetMapping("/permissions/resource/{resource}")
    @Operation(summary = "Liste les permissions par ressource")
    public ResponseEntity<List<PermissionDTO>> getPermissionsByResource(@PathVariable String resource) {
        return ResponseEntity.ok(permissionService.getPermissionsByResource(resource));
    }

    @PostMapping("/permissions")
    @Operation(summary = "Crée une nouvelle permission")
    public ResponseEntity<PermissionDTO> createPermission(@Valid @RequestBody PermissionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.createPermission(dto));
    }

    @PutMapping("/permissions/{id}")
    @Operation(summary = "Met à jour une permission")
    public ResponseEntity<PermissionDTO> updatePermission(
            @PathVariable String id,
            @Valid @RequestBody PermissionDTO dto) {
        return ResponseEntity.ok(permissionService.updatePermission(id, dto));
    }

    @DeleteMapping("/permissions/{id}")
    @Operation(summary = "Supprime une permission")
    public ResponseEntity<Void> deletePermission(@PathVariable String id) {
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Role Management ==========

    @GetMapping("/roles")
    @Operation(summary = "Liste tous les rôles")
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        return ResponseEntity.ok(permissionService.getAllRoles());
    }

    @GetMapping("/roles/{id}")
    @Operation(summary = "Récupère un rôle par ID")
    public ResponseEntity<RoleDTO> getRoleById(@PathVariable String id) {
        return ResponseEntity.ok(permissionService.getRoleById(id));
    }

    @PostMapping("/roles")
    @Operation(summary = "Crée un nouveau rôle")
    public ResponseEntity<RoleDTO> createRole(@Valid @RequestBody RoleDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionService.createRole(dto));
    }

    @PutMapping("/roles/{id}")
    @Operation(summary = "Met à jour un rôle")
    public ResponseEntity<RoleDTO> updateRole(
            @PathVariable String id,
            @Valid @RequestBody RoleDTO dto) {
        return ResponseEntity.ok(permissionService.updateRole(id, dto));
    }

    @DeleteMapping("/roles/{id}")
    @Operation(summary = "Supprime un rôle")
    public ResponseEntity<Void> deleteRole(@PathVariable String id) {
        permissionService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    // ========== Role-Permission Assignment ==========

    @PostMapping("/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Assigne une permission à un rôle")
    public ResponseEntity<RoleDTO> assignPermissionToRole(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        return ResponseEntity.ok(permissionService.assignPermissionToRole(roleId, permissionId));
    }

    @DeleteMapping("/roles/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Retire une permission d'un rôle")
    public ResponseEntity<RoleDTO> removePermissionFromRole(
            @PathVariable String roleId,
            @PathVariable String permissionId) {
        return ResponseEntity.ok(permissionService.removePermissionFromRole(roleId, permissionId));
    }

    @GetMapping("/roles/{roleId}/permissions")
    @Operation(summary = "Liste les permissions d'un rôle")
    public ResponseEntity<Set<PermissionDTO>> getPermissionsForRole(@PathVariable String roleId) {
        return ResponseEntity.ok(permissionService.getPermissionsForRole(roleId));
    }
}