package com.tarantulacommunity.tarantulasocial.security.rate_limit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.security.rate_limit.properties.RateLimitProperties;
import com.tarantulacommunity.tarantulasocial.security.rate_limit.properties.RateLimitRule;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;

import java.time.Duration;
import java.util.List;

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
public class RateLimitConfig {

    @Value("${cache.ip-rate-limit.maximum-size:50000}")
    private long ipRateLimitMaxSize;

    @Value("${cache.ip-rate-limit.expire-after-write:2h}")
    private String ipRateLimitExpireAfterWrite;

    @Value("${cache.user-rate-limit.maximum-size:50000}")
    private long userRateLimitMaxSize;

    @Value("${cache.user-rate-limit.expire-after-write:2h}")
    private String userRateLimitExpireAfterWrite;

    /**
     * IP-keyed buckets, for auth endpoints hit before a principal exists.
     * expireAfterAccess is the memory-leak guard: an IP that stops making
     * requests gets evicted instead of living in the cache forever.
     */
    @Bean
    public Cache<String, Bucket> ipRateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(parseDuration(ipRateLimitExpireAfterWrite))
                .maximumSize(ipRateLimitMaxSize)
                .build();
    }

    /**
     * User-keyed buckets, for authenticated endpoints. Same eviction logic.
     */
    @Bean
    public Cache<String, Bucket> userRateLimitCache() {
        return Caffeine.newBuilder()
                .expireAfterAccess(parseDuration(userRateLimitExpireAfterWrite))
                .maximumSize(userRateLimitMaxSize)
                .build();
    }

    private Duration parseDuration(String duration) {
        if (duration == null || duration.isBlank()) {
            return Duration.ofHours(2); // default
        }
        if (duration.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(duration.replace("h", "")));
        } else if (duration.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(duration.replace("m", "")));
        } else if (duration.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(duration.replace("s", "")));
        }
        return Duration.ofHours(2); // default
    }

    @Bean
    public List<RateLimitRule> ipRateLimitRules(RateLimitProperties props) {
        return List.of(
                new RateLimitRule("login", HttpMethod.POST, "/api/auth/login",
                        props.getLogin().getCapacity(), props.getLogin().getRefillPeriod()),
                new RateLimitRule("register", HttpMethod.POST, "/api/auth/register",
                        props.getRegister().getCapacity(), props.getRegister().getRefillPeriod()),
                new RateLimitRule("forgot-password", HttpMethod.POST, "/api/auth/forgot-password",
                        props.getForgotPassword().getCapacity(), props.getForgotPassword().getRefillPeriod()),
                new RateLimitRule("reset-password", HttpMethod.POST, "/api/auth/reset-password",
                        props.getResetPassword().getCapacity(), props.getResetPassword().getRefillPeriod()),
                new RateLimitRule("refresh", HttpMethod.POST, "/api/auth/refresh",
                        props.getRefresh().getCapacity(), props.getRefresh().getRefillPeriod())
        );
    }

    @Bean
    public List<RateLimitRule> userRateLimitRules(RateLimitProperties props) {
        return List.of(
                new RateLimitRule("message-send", HttpMethod.POST, "/api/messages",
                        props.getMessageSend().getCapacity(), props.getMessageSend().getRefillPeriod()),

                // All three share the SAME name -> same bucket per user, so a
                // user can't get 3x the effective budget by mixing operations.
                new RateLimitRule("collection-write", HttpMethod.POST, "/api/collection",
                        props.getCollectionWrite().getCapacity(), props.getCollectionWrite().getRefillPeriod()),
                new RateLimitRule("collection-write", HttpMethod.PUT, "/api/collection/*",
                        props.getCollectionWrite().getCapacity(), props.getCollectionWrite().getRefillPeriod()),
                new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/collection/*",
                        props.getCollectionWrite().getCapacity(), props.getCollectionWrite().getRefillPeriod()),

                new RateLimitRule("profile-update", HttpMethod.PUT, "/api/users/*/profile",
                        props.getProfileUpdate().getCapacity(), props.getProfileUpdate().getRefillPeriod()),
                new RateLimitRule("favorite-update", HttpMethod.PUT, "/api/users/*/favorite",
                        props.getProfileUpdate().getCapacity(), props.getProfileUpdate().getRefillPeriod())
        );
    }
}