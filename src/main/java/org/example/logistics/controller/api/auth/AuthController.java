package org.example.logistics.controller.api.auth;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.logistics.dto.auth.AuthResponse;
import org.example.logistics.dto.auth.LoginRequest;
import org.example.logistics.dto.auth.RefreshTokenRequest;
import org.example.logistics.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("gggggggggggggggggggggggggggggggg");
        authService.logout(request);
        log.info("sssssshssssssssssssssssssssssssssssssssssssssssssss");
        return ResponseEntity.ok().body("Déconnexion réussie");
    }
}

