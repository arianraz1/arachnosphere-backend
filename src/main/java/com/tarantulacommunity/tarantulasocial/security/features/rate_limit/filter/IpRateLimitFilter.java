package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.util.ClientIpResolver;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.List;

/**
 * Runs BEFORE authentication. Keyed by client IP. Only guards the handful of
 * public/auth endpoints listed in ipRateLimitRules — every other request
 * passes through untouched.
 *
 * Exceptions thrown here are NOT caught by @ControllerAdvice on their own,
 * since this runs outside the DispatcherServlet. We hand the exception to
 * Spring's own "handlerExceptionResolver" bean so it gets routed through the
 * existing GlobalExceptionHandler and comes back in the same ErrorResponse
 * shape as every other error in the app.
 */
@Component
public class IpRateLimitFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final Cache<String, Bucket> ipRateLimitCache;
    private final List<RateLimitRule> ipRateLimitRules;
    private final HandlerExceptionResolver exceptionResolver;

    public IpRateLimitFilter(
            @Qualifier("ipRateLimitCache") Cache<String, Bucket> ipRateLimitCache,
            @Qualifier("ipRateLimitRules") List<RateLimitRule> ipRateLimitRules,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver
    ) {
        this.ipRateLimitCache = ipRateLimitCache;
        this.ipRateLimitRules = ipRateLimitRules;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        RateLimitRule rule = findMatchingRule(request);
        if (rule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = ClientIpResolver.resolve(request);
        String key = "ip:" + clientIp + ":" + rule.name();

        Bucket bucket = ipRateLimitCache.get(key, k -> Bucket.builder()
                .addLimit(rule.toBandwidth())
                .build());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        // Add rate limit headers to response
        response.setHeader("X-RateLimit-Limit", String.valueOf(rule.capacity()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
        } else {
            long waitSeconds = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            exceptionResolver.resolveException(request, response, null,
                    new GlobalExceptionHandler.RateLimitExceededException("Too many requests. Please try again later.", waitSeconds));
        }
    }

    private RateLimitRule findMatchingRule(HttpServletRequest request) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String path = request.getRequestURI();
        for (RateLimitRule rule : ipRateLimitRules) {
            if (rule.matches(method, path, PATH_MATCHER)) {
                return rule;
            }
        }
        return null;
    }
}