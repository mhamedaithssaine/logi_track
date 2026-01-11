package org.example.logistics.service;

import lombok.extern.slf4j.Slf4j;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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

        log.info("AUTH_LOGIN_PROCESS email={}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->  {
                    log.warn("AUTH_LOGIN_FAILED email={} reason=USER_NOT_FOUND",
                        request.getEmail());
                return ResourceNotFoundException.withEmail("User", request.getEmail());
                });

        if (!user.getActive()) {
            log.warn("AUTH_LOGIN_FAILED userId={} reason=USER_DISABLED",
                    user.getId());

            throw new ConflictException("Compte désactivé");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {

            log.warn("AUTH_LOGIN_FAILED userId={} reason=BAD_PASSWORD",
                    user.getId());
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

        log.info("AUTH_LOGIN_AUTHENTICATED userId={} role={}",
                user.getId(),
                user.getRole().name());

        String accessToken = jwtService.generateAccessToken(userDetails);

        log.info("AUTH_LOGIN_TOKENS_CREATED userId={}", user.getId());


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

        log.info("AUTH_REFRESH_PROCESS");


        User user = refreshToken.getUser();
        if (!user.getActive()) {

            log.warn("AUTH_REFRESH_FAILED userId={} reason=USER_DISABLED",
                    user.getId());

            throw new ConflictException("Compte désactivé");
        }

        refreshTokenService.revokeRefreshToken(request.getRefreshToken());

        log.info("AUTH_REFRESH_OLD_TOKEN_REVOKED userId={}", user.getId());


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String newAccessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        log.info("AUTH_REFRESH_NEW_TOKENS_CREATED userId={}", user.getId());

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
        log.info("AUTH_LOGOUT_PROCESS");

        try {
            refreshTokenService.revokeRefreshToken(request.getRefreshToken());
            log.info("AUTH_LOGOUT_REFRESH_REVOKED");

        } catch (BusinessException e) {
            log.error("AUTH_LOGOUT_FAILED reason={}", e.getMessage());
            throw new BusinessException("Erreur lors de la déconnexion : " + e.getMessage());
        }
    }
}

