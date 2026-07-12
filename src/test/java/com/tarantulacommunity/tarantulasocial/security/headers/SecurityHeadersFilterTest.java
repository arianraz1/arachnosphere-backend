package com.tarantulacommunity.tarantulasocial.security.headers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHeadersFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private SecurityHeadersFilter securityHeadersFilter;

    @BeforeEach
    void setUp() {
        securityHeadersFilter = new SecurityHeadersFilter();
        ReflectionTestUtils.setField(securityHeadersFilter, "baseUrl", "http://localhost:8080");
    }

    @Test
    void doFilterInternal_shouldAddContentSecurityPolicy() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        String expectedCsp = "default-src 'self'; " +
                "frame-ancestors 'none'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data:;";
        verify(response).setHeader("Content-Security-Policy", expectedCsp);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldAddXFrameOptionsDeny() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Frame-Options", "DENY");
    }

    @Test
    void doFilterInternal_shouldAddXContentTypeOptionsNosniff() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Content-Type-Options", "nosniff");
    }

    @Test
    void doFilterInternal_shouldAddXXSSProtection() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-XSS-Protection", "1; mode=block");
    }

    @Test
    void doFilterInternal_shouldAddReferrerPolicy() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    void doFilterInternal_shouldAddPermissionsPolicy() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("Permissions-Policy"), any(String.class));
    }

    @Test
    void doFilterInternal_shouldRemoveServerHeader() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("Server", "");
    }

    @Test
    void doFilterInternal_shouldAddHSTS_inProduction() throws ServletException, IOException {
        ReflectionTestUtils.setField(securityHeadersFilter, "baseUrl", "https://api.arachnosphere.com");

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("Strict-Transport-Security"), any(String.class));
    }

    @Test
    void doFilterInternal_shouldNotAddHSTS_inDevelopment() throws ServletException, IOException {
        ReflectionTestUtils.setField(securityHeadersFilter, "baseUrl", "http://localhost:8080");

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response, never()).setHeader(eq("Strict-Transport-Security"), any(String.class));
    }

    @Test
    void doFilterInternal_shouldNotAddHSTS_with127_0_0_1() throws ServletException, IOException {
        ReflectionTestUtils.setField(securityHeadersFilter, "baseUrl", "http://127.0.0.1:8080");

        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(response, never()).setHeader(eq("Strict-Transport-Security"), any(String.class));
    }

    @Test
    void doFilterInternal_shouldCallFilterChain() throws ServletException, IOException {
        securityHeadersFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_shouldHandleExceptionInFilterChain() throws ServletException, IOException {
        doThrow(new IOException("Test exception")).when(filterChain).doFilter(request, response);

        try {
            securityHeadersFilter.doFilterInternal(request, response, filterChain);
        } catch (IOException e) {
            // Expected exception
        }

        verify(response).setHeader("X-Frame-Options", "DENY");
    }
}
