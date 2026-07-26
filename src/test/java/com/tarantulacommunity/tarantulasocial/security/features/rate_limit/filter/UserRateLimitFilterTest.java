package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.properties.RateLimitRule;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.service.RateLimitService;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserRateLimitFilter.
 * Verifies user-based rate limiting, authentication checks, admin bypass, and bucket creation.
 */
@ExtendWith(MockitoExtension.class)
class UserRateLimitFilterTest {

    private UserRateLimitFilter filter;
    private Cache<String, Bucket> cache;
    private List<RateLimitRule> rules;
    private RateLimitRule defaultRateLimitRule;
    private RateLimitService rateLimitService;

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
                new RateLimitRule("message-send", HttpMethod.POST, "/api/v1/messages", 30, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.POST, "/api/v1/collection", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.PUT, "/api/v1/collection/*", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/v1/collection/*", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.PUT, "/api/v1/collection/*/images/*/featured", 60, Duration.ofMinutes(1)),
                new RateLimitRule("collection-write", HttpMethod.DELETE, "/api/v1/collection/*/images/*", 60, Duration.ofMinutes(1)),
                new RateLimitRule("profile-update", HttpMethod.PUT, "/api/v1/users/*/profile", 20, Duration.ofMinutes(1)),
                new RateLimitRule("favorite-update", HttpMethod.PUT, "/api/v1/users/*/favorite", 20, Duration.ofMinutes(1)),
                new RateLimitRule("image-upload", HttpMethod.POST, "/api/v1/users/*/profile/image", 10, Duration.ofMinutes(1)),
                new RateLimitRule("image-upload", HttpMethod.POST, "/api/v1/users/*/favorite/image", 10, Duration.ofMinutes(1)),
                new RateLimitRule("image-upload", HttpMethod.POST, "/api/v1/collection/**/images", 10, Duration.ofMinutes(1)),
                new RateLimitRule("admin-sync", HttpMethod.POST, "/api/v1/admin/species/sync/**", 5, Duration.ofMinutes(5))
        );
        defaultRateLimitRule = new RateLimitRule("default", null, "/**", 300, Duration.ofMinutes(1));
        rateLimitService = new RateLimitService();

        filter = new UserRateLimitFilter(cache, rules, defaultRateLimitRule, exceptionResolver, rateLimitService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowRequestWhenNotAuthenticatedAndNoRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldFallBackToDefaultRuleWhenNoSpecificRuleMatches() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/api/v1/unknown");
        setAuthentication(1L, "USER");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("default");
    }

    @Test
    void shouldSkipFilterWhenNotAuthenticated() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldEnforceLimitForAdminUserOnNonExemptEndpoint() throws Exception {
        // Admin bypass was removed for consistency — admins are throttled
        // like anyone else on ordinary endpoints.
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");
        setAuthentication(1L, "ADMIN");

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldEnforceAdminSyncLimitForAdminUser() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/admin/species/sync/photos");
        setAuthentication(1L, "ADMIN");

        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldNotBypassLimitForRegularUser() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");
        setAuthentication(2L, "USER");

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentUsers() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        setAuthentication(1L, "USER");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        setAuthentication(2L, "USER");
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(31)).doFilter(request, response);
    }

    @Test
    void shouldCreateSeparateBucketsForDifferentRules() throws Exception {
        setAuthentication(1L, "USER");

        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setMethod("POST");
        request.setRequestURI("/api/v1/collection");
        for (int i = 0; i < 60; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(90)).doFilter(request, response);
    }

    @Test
    void shouldShareBucketBetweenSameNamedRulesForDifferentMethods() throws Exception {
        setAuthentication(1L, "USER");

        request.setMethod("POST");
        request.setRequestURI("/api/v1/collection");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setMethod("PUT");
        request.setRequestURI("/api/v1/collection/1");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setMethod("DELETE");
        request.setRequestURI("/api/v1/collection/1");
        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldShareBucketBetweenCollectionWriteAndImageSubResourceMutations() throws Exception {
        setAuthentication(1L, "USER");

        request.setMethod("PUT");
        request.setRequestURI("/api/v1/collection/1/images/5/featured");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setMethod("DELETE");
        request.setRequestURI("/api/v1/collection/1/images/5");
        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        // 61st combined collection-write operation should be rejected
        request.setMethod("POST");
        request.setRequestURI("/api/v1/collection");
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldContainUserIdInCacheKey() throws Exception {
        setAuthentication(123L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("user:123");
    }

    @Test
    void shouldContainRuleNameInCacheKey() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("message-send");
    }

    @Test
    void shouldHandlePathPatternMatching() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/42/profile");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("profile-update");
    }

    @Test
    void shouldFallBackToDefaultForPathOutsidePattern() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/42/setting");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains("default");
    }

    @Test
    void shouldRejectWithRetryAfterMessage() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

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
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldReuseExistingBucketForSameUserAndRule() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterFirstRequest = cache.asMap().size();

        filter.doFilterInternal(request, response, filterChain);
        int cacheSizeAfterSecondRequest = cache.asMap().size();

        assertThat(cacheSizeAfterFirstRequest).isEqualTo(cacheSizeAfterSecondRequest);
        assertThat(cacheSizeAfterFirstRequest).isEqualTo(1);
    }

    @Test
    void shouldExtractUserIdFromCustomUserDetails() throws Exception {
        setAuthentication(99L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap().keySet().stream().findFirst().get())
                .contains(":99:");
    }

    @Test
    void shouldEnforceProfileUpdateLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/1/profile");

        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldEnforceFavoriteUpdateLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/1/favorite");

        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldEnforceImageUploadLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/collection/1/images");

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldShareImageUploadBucketAcrossProfileAndFavoriteImages() throws Exception {
        setAuthentication(1L, "USER");

        request.setMethod("POST");
        request.setRequestURI("/api/v1/users/1/profile/image");
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        request.setRequestURI("/api/v1/users/1/favorite/image");
        for (int i = 0; i < 5; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        filter.doFilterInternal(request, response, filterChain);

        verify(exceptionResolver).resolveException(eq(request), eq(response), isNull(),
                any(GlobalExceptionHandler.RateLimitExceededException.class));
    }

    @Test
    void shouldAllowProfileUpdateWithinLimit() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/1/profile");

        for (int i = 0; i < 20; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }

        verify(filterChain, times(20)).doFilter(request, response);
        verify(exceptionResolver, never()).resolveException(any(), any(), any(), any());
    }

    @Test
    void shouldHandleMultipleBuckets() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");
        filter.doFilterInternal(request, response, filterChain);

        setAuthentication(2L, "USER");
        request.setMethod("PUT");
        request.setRequestURI("/api/v1/users/2/profile");
        filter.doFilterInternal(request, response, filterChain);

        assertThat(cache.asMap()).hasSize(2);
    }

    // --- reset header behavior (documented, not a bug) ---

    @Test
    void shouldReportZeroResetHeader_onSuccessfulConsumption() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getHeader("X-RateLimit-Reset-User")).isEqualTo("0");
    }

    @Test
    void shouldReportPositiveResetHeader_whenRejected() throws Exception {
        setAuthentication(1L, "USER");
        request.setMethod("POST");
        request.setRequestURI("/api/v1/messages");

        for (int i = 0; i < 30; i++) {
            filter.doFilterInternal(request, response, filterChain);
        }
        filter.doFilterInternal(request, response, filterChain);

        long reset = Long.parseLong(response.getHeader("X-RateLimit-Reset-User"));
        assertThat(reset).isGreaterThan(0);
    }

    private void setAuthentication(Long userId, String role) {
        UserEntity user = new UserEntity();
        user.setId(userId);
        user.setEmail("test" + userId + "@example.com");
        user.setPassword("hashed");

        Long adminUserId = "ADMIN".equals(role) ? userId : null;
        CustomUserDetails userDetails = new CustomUserDetails(user, adminUserId);

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.isAuthenticated()).thenReturn(true);
        lenient().when(auth.getPrincipal()).thenReturn(userDetails);
        lenient().when(auth.getAuthorities()).thenAnswer(invocation ->
                List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}