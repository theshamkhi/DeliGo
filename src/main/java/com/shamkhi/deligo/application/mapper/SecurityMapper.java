package com.shamkhi.deligo.application.mapper;

import com.shamkhi.deligo.domain.security.dto.PermissionDTO;
import com.shamkhi.deligo.domain.security.dto.RoleDTO;
import com.shamkhi.deligo.domain.security.dto.UserDTO;
import com.shamkhi.deligo.domain.security.model.Permission;
import com.shamkhi.deligo.domain.security.model.Role;
import com.shamkhi.deligo.domain.security.model.User;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SecurityMapper {

    // ========== Permission Mappings ==========

    @Mapping(target = "id", ignore = true)
    Permission toPermissionEntity(PermissionDTO dto);

    PermissionDTO toPermissionDTO(Permission entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updatePermissionEntity(PermissionDTO dto, @MappingTarget Permission entity);

    // ========== Role Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    Role toRoleEntity(RoleDTO dto);

    @Mapping(target = "permissions", source = "permissions", qualifiedByName = "permissionsToNames")
    RoleDTO toRoleDTO(Role entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "permissions", ignore = true)
    void updateRoleEntity(RoleDTO dto, @MappingTarget Role entity);

    // ========== User Mappings ==========

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clientExpediteur", ignore = true)
    @Mapping(target = "livreur", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    User toUserEntity(UserDTO dto);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "rolesToNames")
    @Mapping(target = "permissions", source = "roles", qualifiedByName = "rolesToPermissionNames")
    @Mapping(target = "clientExpediteurId", expression = "java(entity.getClientExpediteur() != null ? entity.getClientExpediteur().getId() : null)")
    @Mapping(target = "livreurId", expression = "java(entity.getLivreur() != null ? entity.getLivreur().getId() : null)")
    UserDTO toUserDTO(User entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "clientExpediteur", ignore = true)
    @Mapping(target = "livreur", ignore = true)
    @Mapping(target = "dateCreation", ignore = true)
    @Mapping(target = "dateModification", ignore = true)
    void updateUserEntity(UserDTO dto, @MappingTarget User entity);

    // ========== Custom Mapping Methods ==========

    @Named("permissionsToNames")
    default Set<String> permissionsToNames(Set<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    @Named("rolesToNames")
    default Set<String> rolesToNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    @Named("rolesToPermissionNames")
    default Set<String> rolesToPermissionNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }
}