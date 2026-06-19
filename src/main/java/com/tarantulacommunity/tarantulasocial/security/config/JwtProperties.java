package com.tarantulacommunity.tarantulasocial.security.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated // Keeps @NotBlank checking active!
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        @NotBlank String secret,
        Duration accessTokenExpiration,
        Duration refreshTokenExpiration
) {}