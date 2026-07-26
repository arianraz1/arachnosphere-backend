package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Externalized rate limit configuration. Bind target for the `rate-limit.*`
 * keys in application.yml. Using Duration fields lets Spring Boot parse
 * simple suffixed values like "15m", "1h", "1m" out of the box.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    // --- IP-based limits (unauthenticated / auth endpoints) ---
    private Limit login = new Limit(10, Duration.ofMinutes(15));
    private Limit register = new Limit(3, Duration.ofHours(1));
    private Limit forgotPassword = new Limit(5, Duration.ofHours(1));
    private Limit resetPassword = new Limit(10, Duration.ofHours(1));
    private Limit refresh = new Limit(15, Duration.ofHours(1));
    private Limit resendVerification = new Limit(5, Duration.ofHours(1));
    private Limit csrf = new Limit(20, Duration.ofMinutes(1));
    private Limit verifyEmail = new Limit(5, Duration.ofHours(1));

    // --- User-based limits (authenticated endpoints) ---
    private Limit messageSend = new Limit(8, Duration.ofMinutes(1));
    private Limit collectionWrite = new Limit(20, Duration.ofMinutes(1));
    private Limit profileUpdate = new Limit(8, Duration.ofMinutes(1));
    private Limit imageUpload = new Limit(5, Duration.ofMinutes(1));
    private Limit adminSpeciesSync = new Limit(5, Duration.ofMinutes(5));
    private Limit threadCreation = new Limit(18, Duration.ofHours(1));

    // --- Default limit ---
    private Limit defaultLimit = new Limit(150, Duration.ofMinutes(1));

    @Getter
    @Setter
    public static class Limit {
        private int capacity;
        private Duration refillPeriod;

        public Limit() {
        }

        public Limit(int capacity, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
        }
    }
}
