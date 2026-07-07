package com.tarantulacommunity.tarantulasocial.security.refresh_token.util;

import com.tarantulacommunity.tarantulasocial.security.auth.properties.JwtProperties;
import com.tarantulacommunity.tarantulasocial.security.refresh_token.properties.CookieProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_PATH = "/api/v1/auth/refresh";
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
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProps.secure())
                .sameSite("Lax")
                .path(path);

        if (cookieProps.domain() != null && !cookieProps.domain().isBlank()) {
            builder.domain(cookieProps.domain());
        }

        return builder;
    }

    public ResponseCookie clearCsrfToken() {
        return ResponseCookie.from("XSRF-TOKEN", "")
                .httpOnly(false)
                .secure(cookieProps.secure())
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
    }
}
