package com.shamkhi.deligo.domain.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String type = "Bearer";
    private String id;
    private String username;
    private String email;
    private String nom;
    private String prenom;
    private Set<String> roles;
    private Set<String> permissions;
}