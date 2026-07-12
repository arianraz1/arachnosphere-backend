package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties;

import io.github.bucket4j.Bandwidth;
import org.springframework.http.HttpMethod;

import java.time.Duration;

/**
 * One guarded endpoint.
 *
 * `name` is the bucket-key component — give two rules the SAME name if you
 * want them to share one budget (e.g. collection create/update/delete all
 * draw from a single "collection-write" bucket per user, rather than each
 * getting its own independent 60/min allowance).
 */
public record RateLimitRule(
        String name,
        HttpMethod method,
        String pathPattern,
        int capacity,
        Duration refillPeriod
) {
    public Bandwidth toBandwidth() {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refillPeriod)
                .build();
    }

    public boolean matches(HttpMethod requestMethod, String requestPath, org.springframework.util.AntPathMatcher matcher) {
        return this.method.equals(requestMethod) && matcher.match(this.pathPattern, requestPath);
    }
}
