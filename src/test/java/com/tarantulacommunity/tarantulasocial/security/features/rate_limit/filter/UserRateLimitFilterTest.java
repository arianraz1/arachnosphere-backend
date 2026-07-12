package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter.UserRateLimitFilter;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for UserRateLimitFilter.
 * Verifies user-based rate limiting, authentication checks, admin bypass, and bucket creation.
 */
@ExtendWith(MockitoExtension.class)
class UserRateLimitFilterTest {

    private UserRateLimitFilter filter;
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
                new RateLimitRule("message-send", HttpMethod.POST, "/api/messages", 30, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.POST, "/api/collection", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.PUT, "/api/collection/*", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/collection/*", 60, Duration.ofMinutes(1)),
                new RateLimitRule("profile-update", HttpMethod.PUT, "/api/users/*/profile", 20, Duration.ofMinutes(1)),
                new RateLimitRule("image-upload", HttpMethod.POST, "/api/collection/**/images", 10, Duration.ofMinutes(1)),
                new RateLimitRule("admin-sync", HttpMethod.POST, "/api/admin/species/sync*", 5, Duration.ofMinutes(5))
        );

        filter = new UserRateLimitFilter(cache, rules, exceptionResolver);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowRequestWhenNotAuthenticatedAndNoRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldSkipFilterWhenNoRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/unknown");

        setAuthentication(1L, "USER");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldSkipFilterWhenNotAuthenticated() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldBypassLimitForAdminUser() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        setAuthentication(1L, "ADMIN");

        // Exceed capacity multiple times
        for (int i = 0; i < 40; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // All requests should pass (admin bypass)
        verify(filterChain, times(40)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldNotBypassLimitForRegularUser() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        setAuthentication(2L, "USER");

        // Message send has capacity 30
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // 31st request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentUsers() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        // User 1 sends 30 messages (capacity)
        setAuthentication(1L, "USER");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // User 2 sends 1 message (should pass, different bucket)
        setAuthentication(2L, "USER");
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(31)).doFilter(request, response);
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentRules() throws Exception {
        setAuthentication(1L, "USER");

        // Message send: capacity 30
        request.setMethod("POST");
        request.setRequestURI("/api/messages");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // Collection write: capacity 60 (different rule, separate bucket)
        request.setMethod("POST");
        request.setRequestURI("/api/collection");
        for (int i = 0; i < 60; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(90)).doFilter(request, response);
    }

    @Test
    void shouldShareBucketBetweenSameNamedRulesForDifferentMethods() throws Exception {
        setAuthentication(1L, "USER");

        // Collection write rules share same name -> same bucket
        request.setMethod("POST");
        request.setRequestURI("/api/collection");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // PUT request should draw from same bucket
        request.setMethod("PUT");
        request.setRequestURI("/api/collection/1");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // DELETE request should also draw from same bucket
        request.setMethod("DELETE");
        request.setRequestURI("/api/collection/1");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // 61st request to collection-write should be rejected
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldContainUserIdInCacheKey() throws Exception {
        setAuthentication(123L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("user:123");
    }

    @Test
    void shouldContainRuleNameInCacheKey() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("message-send");
    }

    @Test
    void shouldHandlePathPatternMatching() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/users/42/profile");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotMatchPathPatternOutsidePattern() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/users/42/setting");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldRejectWithRetryAfterMessage() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        // Exceed capacity
        for (int i = 0; i < 31; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        ArgumentCaptor<Exception> exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(), exceptionCaptor.capture());

        GlobalExceptionHandler.RateLimitExceededException rateLimitEx =
                (GlobalExceptionHandler.RateLimitExceededException) exceptionCaptor.getValue();
        assertThat(rateLimitEx.getRetryAfterSeconds()).isGreaterThan(0);
    }

    @Test
    void shouldHandleUnauthenticatedContextWithRuleMatch() throws Exception {
        // No authentication set
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReuseExistingBucketForSameUserAndRule() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterFirstRequest = cache.asMap().size();

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterSecondRequest = cache.asMap().size();

        // Cache size should not increase (same bucket reused)
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(cacheSizeAfterSecondRequest);
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(1);
    }

    @Test
    void shouldExtractUserIdFromCustomUserDetails() throws Exception {
        setAuthentication(99L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains(":99:");
    }

    @Test
    void shouldBypassIfRoleAdmin() throws Exception {
        setAuthentication(1L, "ADMIN");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");

        // Try to exceed capacity
        for (int i = 0; i < 35; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(35)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldEnforceProfileUpdateLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/users/1/profile");

        // Profile update has capacity 20
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // 21st request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldEnforceImageUploadLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/collection/1/images");

        // Image upload has capacity 10
        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // 11th request should be rejected
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldAllowProfileUpdateWithinLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/users/1/profile");

        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(20)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldHandleMultipleBuckets() throws Exception {
        // User 1 makes requests
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/messages");
        filter.doFilterInternal(request, response, filterChain);

        // User 2 makes different request
        setAuthentication(2L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/users/2/profile");
        filter.doFilterInternal(request, response, filterChain);

        // Cache should have 2 buckets
        assertThat(cache.asMap()).hasSize(2);
    }

    // Helper method to set authentication
    private void setAuthentication(Long userId, String role) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("test" + userId + "@example.com");
        user.setPassword("hashed");

        // If role is ADMIN, set adminUserId to userId so CustomUserDetails grants ROLE_ADMIN
        // Otherwise, set to null so the user gets ROLE_USER
        Long adminUserId = "ADMIN".equals(role) ? userId : null;

        CustomUserDetails userDetails = new CustomUserDetails(user, adminUserId);

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(userDetails);

        // Use lenient() because not all tests use getAuthorities()
        lenient().when(auth.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}






