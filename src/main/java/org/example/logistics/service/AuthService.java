package org.example.logistics.service;

import org.example.logistics.dto.auth.AuthResponse;
import org.example.logistics.dto.auth.LoginRequest;
import org.example.logistics.dto.auth.RefreshTokenRequest;
import org.example.logistics.entity.RefreshToken;
import org.example.logistics.entity.User;
import org.example.logistics.exception.BusinessException;
import org.example.logistics.exception.ConflictException;
import org.example.logistics.exception.ResourceNotFoundException;
import org.example.logistics.repository.UserRepository;
import org.example.logistics.security.CustomUserDetailsService;
import org.example.logistics.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> ResourceNotFoundException.withEmail("User", request.getEmail()));

        if (!user.getActive()) {
            throw new ConflictException("Compte désactivé");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ConflictException("Mot de passe incorrect");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Authentification réussie")
                .build();
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.getRefreshToken());

        User user = refreshToken.getUser();
        if (!user.getActive()) {
            throw new ConflictException("Compte désactivé");
        }

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .message("Token rafraîchi avec succès")
                .build();
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        try {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        } catch (BusinessException e) {
            throw new BusinessException("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
}

