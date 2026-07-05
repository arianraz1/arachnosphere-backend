package com.tarantulacommunity.tarantulasocial.security.auth.util;

import com.tarantulacommunity.tarantulasocial.security.auth.properties.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("mysecretmysecretmysecretmysecret".getBytes()); // 32 bytes
        JwtProperties props = new JwtProperties(secret, Duration.ofMinutes(5), Duration.ofDays(7));
        jwtUtil = new JwtUtil(props);
    }

    @Test
    void shouldGenerateAndExtractUserId() {
        String token = jwtUtil.generateAccessToken(42L, "user@example.com");
        assertThat(jwtUtil.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtUtil.isValid(token)).isTrue();
    }

    @Test
    void shouldRejectExpiredToken() throws InterruptedException {
        JwtProperties shortExp = new JwtProperties(
                Base64.getEncoder().encodeToString("12345678901234567890123456789012".getBytes()),
                Duration.ofMillis(10), Duration.ofDays(7));
        JwtUtil shortJwt = new JwtUtil(shortExp);
        String token = shortJwt.generateAccessToken(1L, "x");
        Thread.sleep(20);
        assertThat(shortJwt.isValid(token)).isFalse();
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtUtil.generateAccessToken(1L, "x");
        String tampered = token.substring(0, token.length()-2) + "xx";
        assertThat(jwtUtil.isValid(tampered)).isFalse();
    }

    @Test
    void shouldRejectTooShortSecret() {
        assertThatThrownBy(() -> new JwtUtil(new JwtProperties("YWJj", Duration.ZERO, Duration.ZERO)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("256 bits");
    }
}
