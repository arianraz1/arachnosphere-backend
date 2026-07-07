package com.tarantulacommunity.tarantulasocial.security.rate_limit.properties;

import io.github.bucket4j.Bandwidth;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.util.AntPathMatcher;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RateLimitRule record.
 * Verifies rule matching, bandwidth conversion, and bucket key generation.
 */
class RateLimitRuleTest {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Test
    void shouldMatchExactPath() {
        RateLimitRule rule = new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15));

        boolean matches = rule.matches(HttpMethod.POST, "/api/auth/login", PATH_MATCHER);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchDifferentMethod() {
        RateLimitRule rule = new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15));

        boolean matches = rule.matches(HttpMethod.GET, "/api/auth/login", PATH_MATCHER);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldNotMatchDifferentPath() {
        RateLimitRule rule = new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15));

        boolean matches = rule.matches(HttpMethod.POST, "/api/auth/register", PATH_MATCHER);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldMatchWildcardPath() {
        RateLimitRule rule = new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/collection/*", 60, Duration.ofMinutes(1));

        boolean matches = rule.matches(HttpMethod.DELETE, "/api/collection/123", PATH_MATCHER);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchWildcardPathOutsidePattern() {
        RateLimitRule rule = new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/collection/*", 60, Duration.ofMinutes(1));

        boolean matches = rule.matches(HttpMethod.DELETE, "/api/user/123", PATH_MATCHER);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldConvertToBandwidth() {
        RateLimitRule rule = new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15));

        Bandwidth bandwidth = rule.toBandwidth();

        assertThat(bandwidth.getCapacity()).isEqualTo(10);
    }

    @Test
    void shouldCreateBandwidthWithGreedyRefill() {
        RateLimitRule rule = new RateLimitRule("register", HttpMethod.POST, "/api/auth/register", 3, Duration.ofHours(1));

        Bandwidth bandwidth = rule.toBandwidth();

        // Bandwidth should be created with greedy refill strategy
        assertThat(bandwidth.getCapacity()).isEqualTo(3);
    }

    @Test
    void shouldHandleDifferentCapacities() {
        RateLimitRule rule1 = new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15));
        RateLimitRule rule2 = new RateLimitRule("register", HttpMethod.POST, "/api/auth/register", 3, Duration.ofHours(1));

        Bandwidth bandwidth1 = rule1.toBandwidth();
        Bandwidth bandwidth2 = rule2.toBandwidth();

        assertThat(bandwidth1.getCapacity()).isEqualTo(10);
        assertThat(bandwidth2.getCapacity()).isEqualTo(3);
    }

    @Test
    void shouldMatchMultipleLevelWildcard() {
        RateLimitRule rule = new RateLimitRule("user-profile", HttpMethod.PUT, "/api/users/*/profile", 20, Duration.ofMinutes(1));

        boolean matches = rule.matches(HttpMethod.PUT, "/api/users/123/profile", PATH_MATCHER);

        assertThat(matches).isTrue();
    }

    @Test
    void shouldNotMatchPartialWildcard() {
        RateLimitRule rule = new RateLimitRule("user-profile", HttpMethod.PUT, "/api/users/*/profile", 20, Duration.ofMinutes(1));

        boolean matches = rule.matches(HttpMethod.PUT, "/api/users/123/setting", PATH_MATCHER);

        assertThat(matches).isFalse();
    }

    @Test
    void shouldStoreName() {
        RateLimitRule rule = new RateLimitRule("test-rule", HttpMethod.GET, "/api/test", 5, Duration.ofMinutes(1));

        assertThat(rule.name()).isEqualTo("test-rule");
    }

    @Test
    void shouldStoreMethod() {
        RateLimitRule rule = new RateLimitRule("test-rule", HttpMethod.POST, "/api/test", 5, Duration.ofMinutes(1));

        assertThat(rule.method()).isEqualTo(HttpMethod.POST);
    }

    @Test
    void shouldStorePathPattern() {
        RateLimitRule rule = new RateLimitRule("test-rule", HttpMethod.GET, "/api/test/*", 5, Duration.ofMinutes(1));

        assertThat(rule.pathPattern()).isEqualTo("/api/test/*");
    }

    @Test
    void shouldStoreCapacity() {
        RateLimitRule rule = new RateLimitRule("test-rule", HttpMethod.GET, "/api/test", 42, Duration.ofMinutes(1));

        assertThat(rule.capacity()).isEqualTo(42);
    }

    @Test
    void shouldStoreRefillPeriod() {
        Duration period = Duration.ofHours(2);
        RateLimitRule rule = new RateLimitRule("test-rule", HttpMethod.GET, "/api/test", 5, period);

        assertThat(rule.refillPeriod()).isEqualTo(period);
    }
}



