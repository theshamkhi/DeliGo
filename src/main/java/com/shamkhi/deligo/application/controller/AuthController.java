package com.shamkhi.deligo.application.controller;

import com.shamkhi.deligo.domain.security.dto.LoginRequest;
import com.shamkhi.deligo.domain.security.dto.LoginResponse;
import com.shamkhi.deligo.domain.security.dto.RegisterRequest;
import com.shamkhi.deligo.domain.security.dto.UserDTO;
import com.shamkhi.deligo.domain.security.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Endpoints d'authentification et d'inscription")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Connexion d'un utilisateur")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequest request) {
        UserDTO user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/profile")
    @Operation(summary = "Récupère les informations de l'utilisateur connecté")
    public ResponseEntity<UserDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion (côté client uniquement)")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Déconnexion réussie. Veuillez supprimer le token côté client.");
    }
}