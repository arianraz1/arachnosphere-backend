package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
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
import static com.tarantulacommunity.tarantulasocial.TestUtilities.apiBasePath;

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
    private RateLimitRule defaultRateLimitRule;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HandlerExceptionResolver exceptionResolver;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private final String base = apiBasePath();

    @BeforeEach
    void setUp() {
        cache = Caffeine.newBuilder().maximumSize(10).build();
        rules = List.of(
                new RateLimitRule("login", HttpMethod.POST, base + "/auth/login", 10, Duration.ofMinutes(15)),
                new RateLimitRule("register", HttpMethod.POST, base + "/auth/register", 3, Duration.ofHours(1)),
                new RateLimitRule("forgot-password", HttpMethod.POST, base + "/auth/forgot-password", 5, Duration.ofHours(1)),
                new RateLimitRule("reset-password", HttpMethod.POST, base + "/auth/reset-password", 10, Duration.ofHours(1)),
                new RateLimitRule("refresh", HttpMethod.POST, base + "/auth/refresh", 30, Duration.ofHours(1)),
                new RateLimitRule("csrf", HttpMethod.GET, base + "/auth/csrf", 20, Duration.ofMinutes(1)),
                new RateLimitRule("resend-verification", HttpMethod.POST, base + "/auth/resend-verification", 5, Duration.ofHours(1)),
                new RateLimitRule("verify-email", HttpMethod.GET, base + "/auth/verify-email", 10, Duration.ofHours(1))
        );
        defaultRateLimitRule = new RateLimitRule("default", null, "/**", 300, Duration.ofMinutes(1));

        filter = new IpRateLimitFilter(cache, rules, defaultRateLimitRule, exceptionResolver);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldAllowRequestWhenBucketNotExceeded() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldAllowMultipleRequestsUpToCapacity() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(2)).doFilter(request, response);
    }

    @Test
    void shouldRejectRequestWhenBucketExceeded() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/register");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldEnforceResetPasswordLimit() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/reset-password");
        request.setRemoteAddr("192.168.1.1");

        // reset-password capacity is 10
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldAllowResetPasswordWithinLimit() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/reset-password");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(10)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldContainResetPasswordRuleNameInCacheKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/reset-password");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("reset-password");
    }

    @Test
    void shouldEnforceRefreshLimit() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/refresh");
        request.setRemoteAddr("192.168.1.1");

        // refresh capacity is 30
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldAllowRefreshWithinLimit() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/refresh");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(30)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldContainRefreshRuleNameInCacheKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/refresh");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("refresh");
    }

    @Test
    void shouldGiveResetPasswordAndRefreshSeparateBuckets() throws Exception {
        request.setRemoteAddr("192.168.1.1");
        request.setMethod("POST");

        request.setRequestURI(base + "/auth/reset-password");
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setRequestURI(base + "/auth/refresh");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Both should have succeeded fully since they're independent buckets
        verify(filterChain, times(40)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldFallBackToDefaultRuleWhenNoSpecificRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(base + "/auth/some-unknown-endpoint");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("default");
    }

    @Test
    void shouldEnforceDefaultRuleCapacityWhenExceeded() throws Exception {
        List<RateLimitRule> emptyRules = List.of();
        RateLimitRule strictDefault = new RateLimitRule("default", null, "/**", 1, Duration.ofMinutes(1));
        filter = new IpRateLimitFilter(cache, emptyRules, strictDefault, exceptionResolver);

        request.setMethod("GET");
        request.setRequestURI(base + "/species/search/all");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentIps() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");

        request.setRemoteAddr("192.168.1.1");
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setRemoteAddr("192.168.1.2");
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(11)).doFilter(request, response);
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentRules() throws Exception {
        request.setRemoteAddr("192.168.1.1");

        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setRequestURI(base + "/auth/register");
        for (int i = 0; i < 3; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(13)).doFilter(request, response);
    }

    @Test
    void shouldUseXForwardedForHeaderForIpResolution() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");
        request.addHeader("X-Forwarded-For", "203.0.113.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap()).hasSize(1);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("203.0.113.1");
    }

    @Test
    void shouldHandlePathPatternMatching() throws Exception {
        List<RateLimitRule> rulesWithWildcard = List.of(
                new RateLimitRule("collection", HttpMethod.DELETE, base + "/collection/*", 5, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, rulesWithWildcard, defaultRateLimitRule, exceptionResolver);

        request.setMethod("DELETE");
        request.setRequestURI(base + "/collection/123");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldMatchAdminSyncNestedPaths() throws Exception {
        List<RateLimitRule> adminRules = List.of(
                new RateLimitRule("admin-sync", HttpMethod.POST, base + "/admin/species/sync/**", 5, Duration.ofMinutes(5))
        );
        filter = new IpRateLimitFilter(cache, adminRules, defaultRateLimitRule, exceptionResolver);

        request.setMethod("POST");
        request.setRemoteAddr("192.168.1.1");

        request.setRequestURI(base + "/admin/species/sync");
        filter.doFilterInternal(request, response, filterChain);

        request.setRequestURI(base + "/admin/species/sync/photos");
        filter.doFilterInternal(request, response, filterChain);

        request.setRequestURI(base + "/admin/species/sync/photos/full");
        filter.doFilterInternal(request, response, filterChain);

        // All three should draw from the SAME bucket (same rule name)
        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldFallThroughToDefaultForNonMatchingWildcard() throws Exception {
        List<RateLimitRule> rulesWithWildcard = List.of(
                new RateLimitRule("collection", HttpMethod.DELETE, base + "/collection/*", 5, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, rulesWithWildcard, defaultRateLimitRule, exceptionResolver);

        request.setMethod("DELETE");
        request.setRequestURI(base + "/user/123");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("default");
    }

    @Test
    void shouldPassExceptionWithRetryAfterSeconds() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/register");
        request.setRemoteAddr("192.168.1.1");

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
        request.setRequestURI(base + "/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterFirstRequest = cache.asMap().size();

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterSecondRequest = cache.asMap().size();

        assertThat(cacheSizeAfterFirstRequest).isEqualTo(cacheSizeAfterSecondRequest);
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(1);
    }

    @Test
    void shouldCacheBucketPerKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap()).hasSize(1);
    }

    @Test
    void shouldFallBackToDefaultWhenMethodNotMatching() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("default");
    }

    @Test
    void shouldContainRuleNameInCacheKey() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("login");
    }

    @Test
    void shouldBlockAllRequestsWithOneCapacity() throws Exception {
        List<RateLimitRule> strictRules = List.of(
                new RateLimitRule("strict", HttpMethod.POST, base + "/auth/login", 1, Duration.ofMinutes(1))
        );
        filter = new IpRateLimitFilter(cache, strictRules, defaultRateLimitRule, exceptionResolver);

        request.setMethod("POST");
        request.setRequestURI(base + "/auth/login");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldRejectWithMessageContainingRetryAfter() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/register");
        request.setRemoteAddr("192.168.1.1");

        for (int i = 0; i < 4; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(), exceptionCaptor.capture());

        GlobalExceptionHandler.RateLimitExceededException rateLimitEx =
                (GlobalExceptionHandler.RateLimitExceededException) exceptionCaptor.getValue();
        assertThat(rateLimitEx.getRetryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void shouldRateLimitCsrfEndpoint() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(base + "/auth/csrf");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("csrf");
    }

    @Test
    void shouldRateLimitVerifyEmailEndpoint() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(base + "/auth/verify-email");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("verify-email");
    }

    @Test
    void shouldRateLimitResendVerificationEndpoint() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/resend-verification");
        request.setRemoteAddr("192.168.1.1");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("resend-verification");
    }
}


