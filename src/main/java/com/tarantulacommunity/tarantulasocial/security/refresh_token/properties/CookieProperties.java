package com.tarantulacommunity.tarantulasocial.security.refresh_token.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cookie")
public record CookieProperties(
        String domain,
        boolean secure,
        @NotBlank String accessTokenName,
        @NotBlank String refreshTokenName
) {}