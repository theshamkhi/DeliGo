package com.shamkhi.deligo.domain.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private String id;

    @NotBlank(message = "Le nom du rôle est obligatoire")
    private String name;

    private String description;
    private Set<String> permissions;
}