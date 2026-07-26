package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import org.springframework.stereotype.Component;

@Component
public class RateLimitService {

    public ConsumptionProbe tryConsume(Cache<String, Bucket> cache, String key, RateLimitRule rule) {
        Bucket bucket = cache.get(key, k -> Bucket.builder()
                .addLimit(rule.toBandwidth())
                .build());
        return bucket.tryConsumeAndReturnRemaining(1);
    }
}