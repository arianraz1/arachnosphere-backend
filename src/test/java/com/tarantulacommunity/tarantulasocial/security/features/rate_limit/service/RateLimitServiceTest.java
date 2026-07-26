package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;


class RateLimitServiceTest {

    private RateLimitService rateLimitService;
    private Cache<String, Bucket> cache;
    private RateLimitRule rule;

    @BeforeEach
    void setUp() {
        rateLimitService = new RateLimitService();
        cache = Caffeine.newBuilder().maximumSize(10).build();
        rule = new RateLimitRule("test-rule", HttpMethod.POST, "/api/v1/test", 3, Duration.ofMinutes(1));
    }

    @Test
    void shouldConsumeSuccessfully_whenBucketHasCapacity() {
        ConsumptionProbe probe = rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(probe.isConsumed()).isTrue();
    }

    @Test
    void shouldCreateNewBucket_whenKeyNotPresent() {
        assertThat(cache.asMap()).isEmpty();

        rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(cache.asMap()).hasSize(1);
        assertThat(cache.asMap()).containsKey("key1");
    }

    @Test
    void shouldReuseExistingBucket_whenKeyAlreadyPresent() {
        rateLimitService.tryConsume(cache, "key1", rule);
        rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldDecrementRemainingTokens_onEachConsume() {
        ConsumptionProbe first = rateLimitService.tryConsume(cache, "key1", rule);
        ConsumptionProbe second = rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(first.getRemainingTokens()).isEqualTo(2);
        assertThat(second.getRemainingTokens()).isEqualTo(1);
    }

    @Test
    void shouldRejectConsumption_whenCapacityExhausted() {
        rateLimitService.tryConsume(cache, "key1", rule);
        rateLimitService.tryConsume(cache, "key1", rule);
        rateLimitService.tryConsume(cache, "key1", rule);

        ConsumptionProbe fourth = rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(fourth.isConsumed()).isFalse();
    }

    @Test
    void shouldReportPositiveWaitTime_whenRejected() {
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(cache, "key1", rule);
        }

        ConsumptionProbe rejected = rateLimitService.tryConsume(cache, "key1", rule);

        assertThat(rejected.getNanosToWaitForRefill()).isGreaterThan(0);
    }

    @Test
    void shouldTrackSeparateBucketsForDifferentKeys() {
        rateLimitService.tryConsume(cache, "key1", rule);
        rateLimitService.tryConsume(cache, "key2", rule);

        assertThat(cache.asMap()).hasSize(2);
    }

    @Test
    void shouldNotAffectOtherKeys_whenOneKeyExhausted() {
        for (int i = 0; i < 3; i++) {
            rateLimitService.tryConsume(cache, "key1", rule);
        }
        // key1 is now exhausted, key2 should be unaffected
        ConsumptionProbe key1Probe = rateLimitService.tryConsume(cache, "key1", rule);
        ConsumptionProbe key2Probe = rateLimitService.tryConsume(cache, "key2", rule);

        assertThat(key1Probe.isConsumed()).isFalse();
        assertThat(key2Probe.isConsumed()).isTrue();
    }

    @Test
    void shouldRespectDifferentRuleCapacitiesForSameKey() {
        RateLimitRule strictRule = new RateLimitRule("strict", HttpMethod.POST, "/api/v1/strict", 1, Duration.ofMinutes(1));

        ConsumptionProbe first = rateLimitService.tryConsume(cache, "shared-key", strictRule);
        ConsumptionProbe second = rateLimitService.tryConsume(cache, "shared-key", strictRule);

        assertThat(first.isConsumed()).isTrue();
        assertThat(second.isConsumed()).isFalse();
    }

    @Test
    void shouldUseBucketBuiltFromRuleBandwidthOnFirstCreation() {
        RateLimitRule largeRule = new RateLimitRule("large", HttpMethod.POST, "/api/v1/large", 100, Duration.ofMinutes(1));

        ConsumptionProbe probe = rateLimitService.tryConsume(cache, "key1", largeRule);

        assertThat(probe.getRemainingTokens()).isEqualTo(99);
    }
}
