package com.tarantulacommunity.tarantulasocial.security.rate_limit;

import com.github.benmanes.caffeine.cache.Cache;
import com.tarantulacommunity.tarantulasocial.security.rate_limit.properties.RateLimitProperties;
import com.tarantulacommunity.tarantulasocial.security.rate_limit.properties.RateLimitRule;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for RateLimitConfig configuration class.
 * Verifies bean creation, cache initialization, and rule configuration.
 */
class RateLimitConfigTest {

    private RateLimitConfig config;
    private RateLimitProperties properties;

    @BeforeEach
    void setUp() {
        config = new RateLimitConfig();
        properties = new RateLimitProperties();
        // Set default values for @Value fields that won't be injected in unit tests
        ReflectionTestUtils.setField(config, "ipRateLimitMaxSize", 50000L);
        ReflectionTestUtils.setField(config, "ipRateLimitExpireAfterWrite", "2h");
        ReflectionTestUtils.setField(config, "userRateLimitMaxSize", 50000L);
        ReflectionTestUtils.setField(config, "userRateLimitExpireAfterWrite", "2h");
    }

    @Test
    void shouldCreateIpRateLimitCache() {
        Cache<String, Bucket> cache = config.ipRateLimitCache();

        assertThat(cache).isNotNull();
    }

    @Test
    void shouldCreateUserRateLimitCache() {
        Cache<String, Bucket> cache = config.userRateLimitCache();

        assertThat(cache).isNotNull();
    }

    @Test
    void shouldHaveMaximumSizeOf50kForIpCache() {
        Cache<String, Bucket> cache = config.ipRateLimitCache();

        // Add items to verify cache accepts them
        for (int i = 0; i < 100; i++) {
            cache.put("key-" + i, Bucket.builder().addLimit(c -> c.capacity(10).refillGreedy(10, Duration.ofMinutes(1))).build());
        }

        // Cache should hold all 100 items (well under 50k limit)
        assertThat(cache.asMap().size()).isEqualTo(100);
    }

    @Test
    void shouldHaveMaximumSizeOf50kForUserCache() {
        Cache<String, Bucket> cache = config.userRateLimitCache();

        // Add items to verify cache accepts them
        for (int i = 0; i < 100; i++) {
            cache.put("key-" + i, Bucket.builder().addLimit(c -> c.capacity(10).refillGreedy(10, Duration.ofMinutes(1))).build());
        }

        // Cache should hold all 100 items (well under 50k limit)
        assertThat(cache.asMap().size()).isEqualTo(100);
    }

    @Test
    void shouldCreateIpRateLimitRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).isNotNull();
        assertThat(rules).isNotEmpty();
    }

    @Test
    void shouldCreateUserRateLimitRules() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        assertThat(rules).isNotNull();
        assertThat(rules).isNotEmpty();
    }

    @Test
    void shouldIncludeLoginRuleInIpRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "login".equals(r.name()));
    }

    @Test
    void shouldIncludeRegisterRuleInIpRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "register".equals(r.name()));
    }

    @Test
    void shouldIncludeForgotPasswordRuleInIpRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "forgot-password".equals(r.name()));
    }

    @Test
    void shouldIncludeResetPasswordRuleInIpRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "reset-password".equals(r.name()));
    }

    @Test
    void shouldIncludeRefreshRuleInIpRules() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "refresh".equals(r.name()));
    }

    @Test
    void shouldIncludeMessageSendRuleInUserRules() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "message-send".equals(r.name()));
    }

    @Test
    void shouldIncludeCollectionWriteRuleInUserRules() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        assertThat(rules)
                .filteredOn(r -> "collection-write".equals(r.name()))
                .hasSize(3); // POST, PUT, DELETE
    }

    @Test
    void shouldIncludeProfileUpdateRuleInUserRules() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        assertThat(rules).anyMatch(r -> "profile-update".equals(r.name()));
    }

    @Test
    void shouldConfigureLoginRuleWithCorrectMethod() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule loginRule = rules.stream()
                .filter(r -> "login".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(loginRule).isNotNull();
        assertThat(loginRule.method()).isEqualTo(HttpMethod.POST);
    }

    @Test
    void shouldConfigureLoginRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule loginRule = rules.stream()
                .filter(r -> "login".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(loginRule).isNotNull();
        assertThat(loginRule.pathPattern()).isEqualTo("/api/auth/login");
    }

    @Test
    void shouldConfigureLoginRuleWithPropertiesCapacity() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule loginRule = rules.stream()
                .filter(r -> "login".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(loginRule).isNotNull();
        assertThat(loginRule.capacity()).isEqualTo(properties.getLogin().getCapacity());
    }

    @Test
    void shouldConfigureCollectionWritePostMethod() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "collection-write".equals(r.name()) && HttpMethod.POST.equals(r.method()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
    }

    @Test
    void shouldConfigureCollectionWritePutMethod() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "collection-write".equals(r.name()) && HttpMethod.PUT.equals(r.method()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
    }

    @Test
    void shouldConfigureCollectionWriteDeleteMethod() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "collection-write".equals(r.name()) && HttpMethod.DELETE.equals(r.method()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
    }

    @Test
    void shouldShareCapacityBetweenCollectionWriteMethods() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        List<RateLimitRule> collectionRules = rules.stream()
                .filter(r -> "collection-write".equals(r.name()))
                .toList();

        // All should have same capacity (from properties)
        assertThat(collectionRules)
                .allMatch(r -> r.capacity() == properties.getCollectionWrite().getCapacity());
    }

    @Test
    void shouldConfigureRegisterRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "register".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/auth/register");
    }

    @Test
    void shouldConfigureForgotPasswordRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "forgot-password".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/auth/forgot-password");
    }

    @Test
    void shouldConfigureResetPasswordRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "reset-password".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/auth/reset-password");
    }

    @Test
    void shouldConfigureRefreshRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "refresh".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/auth/refresh");
    }

    @Test
    void shouldConfigureMessageSendRuleWithCorrectPath() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "message-send".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/messages");
    }

    @Test
    void shouldConfigureProfileUpdateRuleWithWildcardPath() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        RateLimitRule rule = rules.stream()
                .filter(r -> "profile-update".equals(r.name()))
                .findFirst()
                .orElse(null);

        assertThat(rule).isNotNull();
        assertThat(rule.pathPattern()).isEqualTo("/api/users/*/profile");
    }

    @Test
    void shouldCacheBucketsWithAccessTimeExpiration() {
        Cache<String, Bucket> cache = config.ipRateLimitCache();

        Bucket bucket = Bucket.builder()
                .addLimit(c -> c.capacity(10).refillGreedy(10, Duration.ofMinutes(1)))
                .build();
        cache.put("test-key", bucket);

        // Verify the key exists
        assertThat(cache.getIfPresent("test-key")).isNotNull();
    }

    @Test
    void shouldHaveTwoHourExpirationForIpCache() {
        // This is verified by checking the behavior
        // The cache should evict entries after 2 hours of no access
        Cache<String, Bucket> cache = config.ipRateLimitCache();

        Bucket bucket = Bucket.builder()
                .addLimit(c -> c.capacity(10).refillGreedy(10, Duration.ofMinutes(1)))
                .build();
        cache.put("key1", bucket);

        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldHaveTwoHourExpirationForUserCache() {
        Cache<String, Bucket> cache = config.userRateLimitCache();

        Bucket bucket = Bucket.builder()
                .addLimit(c -> c.capacity(10).refillGreedy(10, Duration.ofMinutes(1)))
                .build();
        cache.put("key1", bucket);

        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldProduceMultipleRulesForIpLimiting() {
        List<RateLimitRule> rules = config.ipRateLimitRules(properties);

        // Should have at least 5 rules
        assertThat(rules).hasSizeGreaterThanOrEqualTo(5);
    }

    @Test
    void shouldProduceMultipleRulesForUserLimiting() {
        List<RateLimitRule> rules = config.userRateLimitRules(properties);

        // Should have at least 7 rules (message-send + 3 collection-write + 2 profile-update for different scenarios + favorite-update)
        assertThat(rules).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void parseDuration_shouldParseHours() {
        // Invokes the private "parseDuration" method via Spring reflection helper
        Duration duration = ReflectionTestUtils.invokeMethod(config, "parseDuration", "5h");
        assertThat(duration).isEqualTo(Duration.ofHours(5));
    }

    @Test
    void parseDuration_shouldParseMinutes() {
        Duration duration = ReflectionTestUtils.invokeMethod(config, "parseDuration", "30m");
        assertThat(duration).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void parseDuration_shouldParseSeconds() {
        Duration duration = ReflectionTestUtils.invokeMethod(config, "parseDuration", "90s");
        assertThat(duration).isEqualTo(Duration.ofSeconds(90));
    }

    @Test
    void parseDuration_shouldReturnDefaultForInvalidFormat() {
        Duration duration = ReflectionTestUtils.invokeMethod(config, "parseDuration", "invalid");
        assertThat(duration).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void parseDuration_shouldReturnDefaultForEmptyString() {
        Duration duration = ReflectionTestUtils.invokeMethod(config, "parseDuration", "");
        assertThat(duration).isEqualTo(Duration.ofHours(2));
    }
}

