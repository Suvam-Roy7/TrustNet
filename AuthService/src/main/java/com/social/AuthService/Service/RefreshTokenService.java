package com.social.AuthService.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.social.AuthService.Entity.RefreshToken;
import com.social.AuthService.Exception.InvalidRefreshTokenException;
import com.social.AuthService.Exception.UserNotFoundException;
import com.social.AuthService.Repository.RefreshTokenRepository;
import com.social.AuthService.Repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final Duration REFRESH_TOKEN_TTL =
            Duration.ofDays(30);

    private static final SecureRandom SECURE_RANDOM =
            new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;

    private final UserRepository userRepository;

    @Transactional
    public String createRefreshToken(UUID userId) {

        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(
                    "User not found");
        }

        // Keep one active refresh token per user.
        refreshTokenRepository.deleteByUserId(userId);

        String rawToken = generateSecureToken();

        RefreshToken refreshToken =
                RefreshToken.builder()
                        .userId(userId)
                        .tokenHash(hashToken(rawToken))
                        .expiresAt(
                                Instant.now()
                                        .plus(REFRESH_TOKEN_TTL))
                        .revoked(false)
                        .build();

        refreshTokenRepository.save(refreshToken);

        // Return the raw token only once to the client.
        return rawToken;
    }

    public RefreshToken validateRefreshToken(
            String rawToken) {

        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidRefreshTokenException(
                    "Refresh token is required");
        }

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByTokenHashAndRevokedFalse(
                                hashToken(rawToken))
                        .orElseThrow(() ->
                                new InvalidRefreshTokenException(
                                        "Invalid refresh token"));

        if (refreshToken.getExpiresAt()
                .isBefore(Instant.now())) {

            throw new InvalidRefreshTokenException(
                    "Refresh token has expired");
        }

        return refreshToken;
    }

    @Transactional
    public void revokeRefreshToken(
            String rawToken) {

        RefreshToken refreshToken =
                validateRefreshToken(rawToken);

        refreshToken.setRevoked(true);

        refreshTokenRepository.save(refreshToken);
    }

    private String generateSecureToken() {

        byte[] tokenBytes = new byte[64];

        SECURE_RANDOM.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String hashToken(String rawToken) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            byte[] hash =
                    digest.digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8));

            return HexFormat.of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    ex);
        }
    }
}