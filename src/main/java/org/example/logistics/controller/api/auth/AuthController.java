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

        log.info("AUTH_LOGIN_ATTEMPT email={} endpoint=/api/auth/login",
                request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("AUTH_LOGIN_SUCCESS userId={} role={} endpoint=/api/auth/login",
                response.getUserId(),
                response.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {

        log.info("AUTH_REFRESH_ATTEMPT endpoint=/api/auth/refresh");

        AuthResponse response = authService.refreshToken(request);

        log.info("AUTH_REFRESH_SUCCESS userId={} role={} endpoint=/api/auth/refresh",
                response.getUserId(),
                response.getRole());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody RefreshTokenRequest request) {

        log.info("AUTH_LOGOUT_ATTEMPT endpoint=/api/auth/logout");

        authService.logout(request);

        log.info("AUTH_LOGOUT_SUCCESS endpoint=/api/auth/logout");

        return ResponseEntity.ok().body("Déconnexion réussie");
    }
}

