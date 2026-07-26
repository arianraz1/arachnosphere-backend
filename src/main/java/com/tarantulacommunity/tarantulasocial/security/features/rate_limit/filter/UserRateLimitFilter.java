package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.service.RateLimitService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

/**
 * Runs AFTER JwtAuthFilter, so SecurityContextHolder already has the
 * resolved principal. Keyed by userId. Skips entirely for unauthenticated
 * requests (nothing to key on — the IP filter or downstream 401/403 already
 * covers those) and for ROLE_ADMIN, per your admin-bypass requirement.
 */
@Component
public class UserRateLimitFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final Cache<String, Bucket> userRateLimitCache;
    private final List<RateLimitRule> userRateLimitRules;
    private final RateLimitRule defaultRateLimitRule;
    private final HandlerExceptionResolver exceptionResolver;
    private final RateLimitService rateLimiterService;

    public UserRateLimitFilter(
            @Qualifier("userRateLimitCache") Cache<String, Bucket> userRateLimitCache,
            @Qualifier("userRateLimitRules") List<RateLimitRule> userRateLimitRules,
            @Qualifier("defaultRateLimitRule") RateLimitRule defaultRateLimitRule,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            RateLimitService rateLimiterService
    ) {
        this.userRateLimitCache = userRateLimitCache;
        this.userRateLimitRules = userRateLimitRules;
        this.defaultRateLimitRule = defaultRateLimitRule;
        this.exceptionResolver = exceptionResolver;
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitRule matchedRule = findMatchingRule(request);
        final RateLimitRule rule = (matchedRule != null) ? matchedRule : defaultRateLimitRule;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = extractUserId(auth);
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = "user:" + userId + ":" + rule.name();
        ConsumptionProbe probe = rateLimiterService.tryConsume(userRateLimitCache, key, rule);

        // See IpRateLimitFilter for a note on why this reset header is only a
        // reliable value on the failure branch, not the success branch.
        response.setHeader("X-RateLimit-Limit-User", String.valueOf(rule.capacity()));
        response.setHeader("X-RateLimit-Remaining-User", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset-User", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            exceptionResolver.resolveException(request, response, null,
                    new GlobalExceptionHandler.RateLimitExceededException("Too many requests. Please slow down.", waitSeconds));
        }
    }

    private Long extractUserId(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getId();
        }
        return null;
    }

    private RateLimitRule findMatchingRule(HttpServletRequest request) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = request.getRequestURI();
        for (RateLimitRule rule : userRateLimitRules) {
            if (rule.matches(method, path, PATH_MATCHER)) {
                return rule;
            }
        }
        return null;
    }
}