package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.service.RateLimitService;
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
    private final RateLimitRule defaultRateLimitRule;
    private final HandlerExceptionResolver exceptionResolver;
    private final RateLimitService rateLimiterService;

    public IpRateLimitFilter(
            @Qualifier("ipRateLimitCache") Cache<String, Bucket> ipRateLimitCache,
            @Qualifier("ipRateLimitRules") List<RateLimitRule> ipRateLimitRules,
            @Qualifier("defaultRateLimitRule") RateLimitRule defaultRateLimitRule,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
            RateLimitService rateLimiterService
    ) {
        this.ipRateLimitCache = ipRateLimitCache;
        this.ipRateLimitRules = ipRateLimitRules;
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

        String clientIp = ClientIpResolver.resolve(request);
        String key = "ip:" + clientIp + ":" + rule.name();

        ConsumptionProbe probe = rateLimiterService.tryConsume(ipRateLimitCache, key, rule);

        // Reset header: nanosToWaitForRefill is only meaningful when the bucket is
        // exhausted (isConsumed() == false); Bucket4j returns 0 for it on a successful
        // consume, so on the happy path this reports "0s" rather than a real refill ETA.
        // Treated here as "seconds until this bucket is empty resets to full availability
        // on failure" — not accurate as a general-purpose reset time on success.
        response.setHeader("X-RateLimit-Limit-IP", String.valueOf(rule.capacity()));
        response.setHeader("X-RateLimit-Remaining-IP", String.valueOf(probe.getRemainingTokens()));
        response.setHeader("X-RateLimit-Reset-IP", String.valueOf(probe.getNanosToWaitForRefill() / 1_000_000_000));

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