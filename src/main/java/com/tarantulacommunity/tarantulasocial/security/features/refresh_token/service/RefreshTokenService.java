package com.tarantulacommunity.tarantulasocial.security.features.refresh_token.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.auth.properties.JwtProperties;
import com.tarantulacommunity.tarantulasocial.security.entity.RefreshTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.features.refresh_token.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Transactional
    public String issue(UserEntity user) {
        String raw = UUID.randomUUID().toString();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(hash(raw));
        entity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenExpiration()));
        entity.setRevoked(false); // Explicit initialization best practice

        refreshTokenRepository.save(entity);
        return raw;
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        String tokenHash = hash(rawToken);
        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token signature lookup failed");
                    return new GlobalExceptionHandler.InvalidRefreshTokenException("Invalid refresh token");
                });

        UserEntity user = stored.getUser();

        if (stored.isRevoked()) {
            log.error("CRITICAL: Reused refresh token detected! Wiping all active sessions.");
            refreshTokenRepository.deleteAllByUser(user);
            refreshTokenRepository.flush(); // Commit instantly to block attacker concurrency
            throw new GlobalExceptionHandler.TokenReusedException();
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            refreshTokenRepository.delete(stored);
            throw new GlobalExceptionHandler.InvalidRefreshTokenException("Refresh token has expired");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newRaw = issue(user);
        return new RotationResult(user, newRaw);
    }

    @Transactional
    public void revokeAll(UserEntity user) {
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() {
        refreshTokenRepository.deleteAllExpired(Instant.now());
        log.info("Purged expired refresh tokens from persistence store");
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable on host JVM", e);
        }
    }

    public record RotationResult(UserEntity user, String newRawToken) {}
}