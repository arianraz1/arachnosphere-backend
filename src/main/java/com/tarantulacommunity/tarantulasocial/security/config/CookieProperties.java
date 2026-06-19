package com.tarantulacommunity.tarantulasocial.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.cookie")
public record CookieProperties(
        @NotBlank String domain,
        boolean secure,
        @NotBlank String accessTokenName,
        @NotBlank String refreshTokenName
) {}