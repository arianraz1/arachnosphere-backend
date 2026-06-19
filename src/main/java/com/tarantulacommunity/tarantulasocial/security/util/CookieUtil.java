package com.tarantulacommunity.tarantulasocial.security.util;

import com.tarantulacommunity.tarantulasocial.security.config.CookieProperties;
import com.tarantulacommunity.tarantulasocial.security.config.JwtProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_PATH = "/api/auth/refresh";
    private final CookieProperties cookieProps;
    private final JwtProperties jwtProps;

    public CookieUtil(CookieProperties cookieProps, JwtProperties jwtProps) {
        this.cookieProps = cookieProps;
        this.jwtProps = jwtProps;
    }

    public ResponseCookie accessTokenCookie(String token) {
        return base(cookieProps.accessTokenName(), token, "/")
                .maxAge(jwtProps.accessTokenExpiration())
                .build();
    }

    public ResponseCookie refreshTokenCookie(String token) {
        return base(cookieProps.refreshTokenName(), token, REFRESH_TOKEN_PATH)
                .maxAge(jwtProps.refreshTokenExpiration())
                .build();
    }

    public ResponseCookie clearAccessToken() {
        return base(cookieProps.accessTokenName(), "", "/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public ResponseCookie clearRefreshToken() {
        return base(cookieProps.refreshTokenName(), "", REFRESH_TOKEN_PATH)
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder base(String name, String value, String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProps.secure())
                .sameSite("Strict")
                .domain(cookieProps.domain())
                .path(path);
    }
}