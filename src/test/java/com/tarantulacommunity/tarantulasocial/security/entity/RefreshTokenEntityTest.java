package com.tarantulacommunity.tarantulasocial.security.entity;

import org.junit.jupiter.api.Test;
import java.time.Instant;
import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenEntityTest {

    @Test
    void shouldDetectExpired() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiresAt(Instant.now().minusSeconds(1));
        assertThat(token.isExpired()).isTrue();
    }

    @Test
    void shouldNotBeExpiredIfFuture() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setExpiresAt(Instant.now().plusSeconds(60));
        assertThat(token.isExpired()).isFalse();
    }
}