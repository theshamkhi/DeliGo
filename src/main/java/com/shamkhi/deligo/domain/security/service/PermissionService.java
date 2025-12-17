package com.shamkhi.deligo.domain.security.service;

import com.shamkhi.deligo.application.mapper.SecurityMapper;
import com.shamkhi.deligo.domain.security.dto.PermissionDTO;
import com.shamkhi.deligo.domain.security.dto.RoleDTO;
import com.shamkhi.deligo.domain.security.model.Permission;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.repository.PermissionRepository;
import com.shamkhi.deligo.domain.security.repository.RoleRepository;
import com.shamkhi.deligo.infrastructure.exception.DuplicateResourceException;
import com.shamkhi.deligo.infrastructure.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final SecurityMapper securityMapper;

    // ========== Permission Management ==========

    public List<PermissionDTO> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(securityMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    public PermissionDTO getPermissionById(String id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée"));
        return securityMapper.toPermissionDTO(permission);
    }

    public PermissionDTO getPermissionByName(String name) {
        Permission permission = permissionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée: " + name));
        return securityMapper.toPermissionDTO(permission);
    }

    public List<PermissionDTO> getPermissionsByResource(String resource) {
        return permissionRepository.findByResource(resource).stream()
                .map(securityMapper::toPermissionDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PermissionDTO createPermission(PermissionDTO dto) {
        log.info("Création d'une nouvelle permission: {}", dto.getName());

        if (permissionRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Cette permission existe déjà");
        }

        Permission permission = securityMapper.toPermissionEntity(dto);
        permission = permissionRepository.save(permission);
        log.info("Permission créée: {}", permission.getName());

        return securityMapper.toPermissionDTO(permission);
    }

    @Transactional
    public PermissionDTO updatePermission(String id, PermissionDTO dto) {
        log.info("Mise à jour de la permission: {}", id);

        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée"));

        if (!permission.getName().equals(dto.getName()) &&
                permissionRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Cette permission existe déjà");
        }

        securityMapper.updatePermissionEntity(dto, permission);
        permission = permissionRepository.save(permission);
        return securityMapper.toPermissionDTO(permission);
    }

    @Transactional
    public void deletePermission(String id) {
        log.info("Suppression de la permission: {}", id);
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée"));
        permissionRepository.delete(permission);
    }

    // ========== Role Management ==========

    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(securityMapper::toRoleDTO)
                .collect(Collectors.toList());
    }

    public RoleDTO getRoleById(String id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));
        return securityMapper.toRoleDTO(role);
    }

    public RoleDTO getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé: " + name));
        return securityMapper.toRoleDTO(role);
    }

    @Transactional
    public RoleDTO createRole(RoleDTO dto) {
        log.info("Création d'un nouveau rôle: {}", dto.getName());

        if (roleRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Ce rôle existe déjà");
        }

        Role role = securityMapper.toRoleEntity(dto);
        role = roleRepository.save(role);
        log.info("Rôle créé: {}", role.getName());

        return securityMapper.toRoleDTO(role);
    }

    @Transactional
    public RoleDTO updateRole(String id, RoleDTO dto) {
        log.info("Mise à jour du rôle: {}", id);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));

        if (!role.getName().equals(dto.getName()) &&
                roleRepository.existsByName(dto.getName())) {
            throw new DuplicateResourceException("Ce rôle existe déjà");
        }

        securityMapper.updateRoleEntity(dto, role);
        role = roleRepository.save(role);
        return securityMapper.toRoleDTO(role);
    }

    @Transactional
    public void deleteRole(String id) {
        log.info("Suppression du rôle: {}", id);
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));
        roleRepository.delete(role);
    }

    // ========== Role-Permission Assignment ==========

    @Transactional
    public RoleDTO assignPermissionToRole(String roleId, String permissionId) {
        log.info("Assignation de la permission {} au rôle {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée"));

        role.getPermissions().add(permission);
        role = roleRepository.save(role);

        return securityMapper.toRoleDTO(role);
    }

    @Transactional
    public RoleDTO removePermissionFromRole(String roleId, String permissionId) {
        log.info("Retrait de la permission {} du rôle {}", permissionId, roleId);

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission non trouvée"));

        role.getPermissions().remove(permission);
        role = roleRepository.save(role);

        return securityMapper.toRoleDTO(role);
    }

    public Set<PermissionDTO> getPermissionsForRole(String roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rôle non trouvé"));

        return role.getPermissions().stream()
                .map(securityMapper::toPermissionDTO)
                .collect(Collectors.toSet());
    }

}