package com.tarantulacommunity.tarantulasocial.security.service;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.config.JwtProperties;
import com.tarantulacommunity.tarantulasocial.security.entity.RefreshTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.exception.InvalidRefreshTokenException;
import com.tarantulacommunity.tarantulasocial.security.exception.TokenReusedException;
import com.tarantulacommunity.tarantulasocial.security.repository.RefreshTokenRepository;
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
        // No cleanup – user may have multiple active sessions
        String raw = UUID.randomUUID().toString();

        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setUser(user);
        entity.setTokenHash(hash(raw));
        entity.setExpiresAt(Instant.now().plus(jwtProperties.refreshTokenExpiration()));

        refreshTokenRepository.save(entity);
        return raw;
    }

    @Transactional
    public RotationResult rotate(String rawToken) {
        String tokenHash = hash(rawToken);
        RefreshTokenEntity stored = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found – invalid or already cleaned up");
                    return new InvalidRefreshTokenException("Invalid refresh token");
                });

        // Token reuse detection
        if (stored.isRevoked()) {
            log.warn("Reused refresh token detected for user {}", stored.getUser().getId());
            refreshTokenRepository.deleteAllByUser(stored.getUser());
            refreshTokenRepository.flush();
            throw new TokenReusedException();
        }

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new InvalidRefreshTokenException("Refresh token has expired");
        }

        // Normal rotation: revoke this token, then issue a new one
        UserEntity user = stored.getUser();
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
        log.info("Purged expired refresh tokens");
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }

    public record RotationResult(UserEntity user, String newRawToken) {}
}
