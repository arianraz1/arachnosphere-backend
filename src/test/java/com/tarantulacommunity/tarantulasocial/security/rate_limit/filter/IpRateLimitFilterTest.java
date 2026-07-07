package com.tarantulacommunity.tarantulasocial.security.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.security.rate_limit.properties.RateLimitRule;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests for IpRateLimitFilter.
 * Verifies IP-based rate limiting, bucket creation, exception handling, and path matching.
 */
@ExtendWith(MockitoExtension.class)
class IpRateLimitFilterTest {

    private IpRateLimitFilter filter;
    private Cache<String, Bucket> cache;
    private List<RateLimitRule> rules;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HandlerExceptionResolver exceptionResolver;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        cache = Caffeine.newBuilder().maximumSize(10).build();
        rules = List.of(
                new RateLimitRule("login", HttpMethod.POST, "/api/auth/login", 10, Duration.ofMinutes(15)),
                new RateLimitRule("register", HttpMethod.POST, "/api/auth/register", 3, Duration.ofHours(1)),
                new RateLimitRule("forgot-password", HttpMethod.POST, "/api/auth/forgot-password", 5, Duration.ofHours(1))
        );

        filter = new IpRateLimitFilter(cache, rules, exceptionResolver);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAllowRequestWhenBucketNotExceeded() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldAllowMultipleRequestsUpToCapacity() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        // First request - should pass
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        // Second request - should still pass (within 10 capacity)
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(2)).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWhenBucketExceeded() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/register");
        request.setRemoteAddr("192.168.1.1");

        // Register has capacity of 3, let's exceed it
        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Fourth request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        // Should have called exception resolver on the 4th request
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldPassThroughRequestWhenNoRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/auth/some-unknown-endpoint");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentIps() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");

        // First IP makes 10 requests (fills bucket)
        request.setRemoteAddr("192.168.1.1");
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Different IP should have its own bucket (capacity 10)
        request.setRemoteAddr("192.168.1.2");
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(11)).doFilter(request, response);
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentRules() throws Exception {
        request.setRemoteAddr("192.168.1.1");

        // Login: capacity 10
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Register: capacity 3 (different rule, separate bucket)
        request.setRequestURI("/api/auth/register");
        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(13)).doFilter(request, response);
    }

    @Test
    void shouldUseXForwardedForHeaderForIpResolution() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.addHeader("X-Forwarded-For", "203.0.113.1");

        filter.doFilterInternal(request, response, filterChain);

        // The key should contain the X-Forwarded-For IP
        assertThat(cache.asMap()).hasSize(1);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("203.0.113.1");
    }

    @Test
    void shouldHandlePathPatternMatching() throws Exception {
        // Setup a rule with wildcard
        List<RateLimitRule> rulesWithWildcard = List.of(
                new RateLimitRule("collection", HttpMethod.DELETE, "/api/collection/*", 5, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, rulesWithWildcard, exceptionResolver);

        request.setMethod("DELETE");
        request.setRequestURI("/api/collection/123");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotMatchWildcardPathOutsidePattern() throws Exception {
        List<RateLimitRule> rulesWithWildcard = List.of(
                new RateLimitRule("collection", HttpMethod.DELETE, "/api/collection/*", 5, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, rulesWithWildcard, exceptionResolver);

        request.setMethod("DELETE");
        request.setRequestURI("/api/user/123");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldPassExceptionWithRetryAfterSeconds() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/register");
        request.setRemoteAddr("192.168.1.1");

        // Exceed capacity
        for (int i = 0; i < 4; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(), exceptionCaptor.capture());

        Exception exception = exceptionCaptor.getValue();
        assertThat(exception).isInstanceOf(GlobalExceptionHandler.RateLimitExceededException.class);
    }

    @Test
    void shouldReuseExistingBucketForSameIpAndRule() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterFirstRequest = cache.asMap().size();

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterSecondRequest = cache.asMap().size();

        // Cache size should not increase (same bucket reused)
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(cacheSizeAfterSecondRequest);
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(1);
    }

    @Test
    void shouldCacheBucketPerKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        // Should have one bucket for: ip:192.168.1.1:login
        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldHandleMethodNotMatching() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldContainRuleNameInCacheKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("login");
    }

    @Test
    void shouldBlockAllRequestsWithOneCapacity() throws Exception {
        List<RateLimitRule> strictRules = List.of(
                new RateLimitRule("strict", HttpMethod.POST, "/api/auth/login", 1, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, strictRules, exceptionResolver);

        request.setMethod("POST");
        request.setRequestURI("/api/auth/login");
        request.setRemoteAddr("192.168.1.1");

        // First request should pass
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        // Second request should be rejected
        filter.doFilterInternal(request, response, filterChain);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldRejectWithMessageContainingRetryAfter() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/auth/register");
        request.setRemoteAddr("192.168.1.1");

        // Exceed capacity
        for (int i = 0; i < 4; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(), exceptionCaptor.capture());

        GlobalExceptionHandler.RateLimitExceededException rateLimitEx =
                (GlobalExceptionHandler.RateLimitExceededException) exceptionCaptor.getValue();
        assertThat(rateLimitEx.getRetryAfterSeconds()).isGreaterThan(0);
    }
}


