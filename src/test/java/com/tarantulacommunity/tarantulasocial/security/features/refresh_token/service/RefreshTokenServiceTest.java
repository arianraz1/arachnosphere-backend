package com.tarantulacommunity.tarantulasocial.security.features.refresh_token.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.auth.properties.JwtProperties;
import com.tarantulacommunity.tarantulasocial.security.entity.RefreshTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.features.refresh_token.repository.RefreshTokenRepository;
import com.tarantulacommunity.tarantulasocial.security.features.refresh_token.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        lenient().when(jwtProperties.refreshTokenExpiration()).thenReturn(Duration.ofDays(7));
    }

    @Test
    void issueShouldSaveTokenAndReturnRaw() {
        String raw = refreshTokenService.issue(user);
        assertThat(raw).isNotBlank();
        verify(refreshTokenRepository).save(any(RefreshTokenEntity.class));
    }

    @Test
    void rotateShouldReturnNewTokenWhenValid() {
        String knownRaw = "test-raw-token";
        String knownHash = computeSha256(knownRaw);

        RefreshTokenEntity stored = new RefreshTokenEntity();
        stored.setTokenHash(knownHash);
        stored.setUser(user);
        stored.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        stored.setRevoked(false);

        given(refreshTokenRepository.findByTokenHash(knownHash)).willReturn(Optional.of(stored));

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(knownRaw);

        assertThat(result.user()).isEqualTo(user);
        assertThat(result.newRawToken()).isNotEqualTo(knownRaw);

        // The service calls save exactly twice: one to mark the old token revoked, one for the new token
        verify(refreshTokenRepository, times(2)).save(any(RefreshTokenEntity.class));

        // Optionally capture the saved entities to assert which is which
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository, times(2)).save(captor.capture());
        List<RefreshTokenEntity> savedEntities = captor.getAllValues();

        assertThat(savedEntities).hasSize(2);
        // The old token should be present and now revoked
        assertThat(savedEntities).anyMatch(e -> e.getTokenHash().equals(knownHash) && e.isRevoked());
        // The new token has a different hash (generated UUID)
        assertThat(savedEntities).anyMatch(e -> !e.getTokenHash().equals(knownHash));
    }

    @Test
    void rotateShouldThrowWhenTokenRevoked() {
        String raw = "reused-token";
        String hash = computeSha256(raw);
        RefreshTokenEntity revoked = new RefreshTokenEntity();
        revoked.setTokenHash(hash);
        revoked.setUser(user);
        revoked.setRevoked(true);
        revoked.setExpiresAt(Instant.now().plus(Duration.ofHours(1)));

        given(refreshTokenRepository.findByTokenHash(hash)).willReturn(Optional.of(revoked));

        assertThatThrownBy(() -> refreshTokenService.rotate(raw))
                .isInstanceOf(GlobalExceptionHandler.TokenReusedException.class);
        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    @Test
    void rotateShouldThrowWhenExpired() {
        String raw = "expired-token";
        String hash = computeSha256(raw);
        RefreshTokenEntity expired = new RefreshTokenEntity();
        expired.setTokenHash(hash);
        expired.setUser(user);
        expired.setRevoked(false);
        expired.setExpiresAt(Instant.now().minus(Duration.ofSeconds(1)));

        given(refreshTokenRepository.findByTokenHash(hash)).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> refreshTokenService.rotate(raw))
                .isInstanceOf(GlobalExceptionHandler.InvalidRefreshTokenException.class)
                .hasMessageContaining("expired");
        verify(refreshTokenRepository).delete(expired);
    }

    @Test
    void revokeAllShouldDeleteAllByUser() {
        refreshTokenService.revokeAll(user);
        verify(refreshTokenRepository).deleteAllByUser(user);
    }

    /** SHA-256 hashing – must match the private method inside RefreshTokenService */
    private String computeSha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encoded = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(encoded);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}