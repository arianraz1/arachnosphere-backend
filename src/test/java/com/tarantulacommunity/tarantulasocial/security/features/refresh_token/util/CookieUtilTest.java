package com.tarantulacommunity.tarantulasocial.security.features.refresh_token.util;

import com.tarantulacommunity.tarantulasocial.security.auth.properties.JwtProperties;
import com.tarantulacommunity.tarantulasocial.security.features.refresh_token.properties.CookieProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieUtilTest {

    private CookieUtil cookieUtil;
    private CookieProperties cookieProps;

    @BeforeEach
    void setUp() {
        cookieProps = new CookieProperties("localhost", false, "access", "refresh");
        JwtProperties jwtProps = new JwtProperties("secret", Duration.ofMinutes(15), Duration.ofDays(7));
        cookieUtil = new CookieUtil(cookieProps, jwtProps);
    }

    @Test
    void shouldBuildAccessTokenCookie() {
        ResponseCookie cookie = cookieUtil.accessTokenCookie("abc");
        assertThat(cookie.getName()).isEqualTo("access");
        assertThat(cookie.getValue()).isEqualTo("abc");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isFalse();

        assertThat(cookie.getSameSite()).isEqualTo("Lax");
    }

    @Test
    void shouldBuildRefreshTokenCookieWithCorrectPath() {
        ResponseCookie cookie = cookieUtil.refreshTokenCookie("def");
        assertThat(cookie.getName()).isEqualTo("refresh");
        assertThat(cookie.getPath()).isEqualTo("/api/v1/auth/refresh");
        assertThat(cookie.getMaxAge()).isPositive();
    }

    @Test
    void shouldBuildClearCookies() {
        ResponseCookie clearAccess = cookieUtil.clearAccessToken();
        assertThat(clearAccess.getMaxAge()).isZero();
        assertThat(clearAccess.getValue()).isEmpty();
    }
}
