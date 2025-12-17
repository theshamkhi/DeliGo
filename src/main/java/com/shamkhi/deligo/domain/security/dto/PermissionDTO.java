package com.shamkhi.deligo.domain.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionDTO {
    private String id;

    @NotBlank(message = "Le nom de la permission est obligatoire")
    private String name;

    private String description;
    private String resource;
    private String action;
}