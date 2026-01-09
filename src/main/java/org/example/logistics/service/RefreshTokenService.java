package org.example.logistics.service;

import org.example.logistics.config.JwtProperties;
import org.example.logistics.entity.RefreshToken;
import org.example.logistics.entity.User;
import org.example.logistics.exception.BusinessException;
import org.example.logistics.repository.RefreshTokenRepository;
import org.example.logistics.repository.UserRepository;
import org.example.logistics.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtProperties jwtProperties;

    @Transactional
    public RefreshToken createRefreshToken(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Utilisateur non trouvé"));

        List<RefreshToken> activeTokens =
                refreshTokenRepository.findAllByUserAndRevokedFalse(user);

        activeTokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(activeTokens);

        UserDetails userDetails =
                userDetailsService.loadUserByUsername(userEmail);

        String tokenValue = jwtService.generateRefreshToken(userDetails);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiryDate(
                        Instant.now().plusMillis(jwtProperties.getRefreshTokenExpiration())
                )
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }


    @Transactional
    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token non trouvé"));

        if (refreshToken.getRevoked()) {
            throw new BusinessException("Refresh token révoqué");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BusinessException("Refresh token expiré");
        }

        if (!jwtService.validateToken(token)) {
            throw new BusinessException("Refresh token invalide");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new BusinessException("Refresh token non trouvé"));
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BusinessException("Refresh token expiré");
        }
    }

}

