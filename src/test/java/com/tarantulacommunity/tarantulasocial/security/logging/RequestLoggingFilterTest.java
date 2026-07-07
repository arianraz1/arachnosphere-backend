package com.tarantulacommunity.tarantulasocial.security.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestLoggingFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private RequestLoggingFilter requestLoggingFilter;

    @BeforeEach
    void setUp() {
        requestLoggingFilter = new RequestLoggingFilter();
    }

    @Test
    void doFilterInternal_shouldAddCorrelationIdHeader_whenNotProvided() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        requestLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Correlation-ID"), any(String.class));
        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
    }

    @Test
    void doFilterInternal_shouldUseExistingCorrelationId_whenProvided() throws ServletException, IOException {
        String existingCorrelationId = "existing-123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(existingCorrelationId);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        requestLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Correlation-ID"), eq(existingCorrelationId));
        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
    }

    @Test
    void doFilterInternal_shouldHandleBlankCorrelationId() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn("   ");
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        requestLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Correlation-ID"), any(String.class));
        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
    }

    @Test
    void doFilterInternal_shouldCallFilterChain() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");

        requestLoggingFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(ContentCachingRequestWrapper.class), any(ContentCachingResponseWrapper.class));
    }

    @Test
    void doFilterInternal_shouldHandleExceptionInFilterChain() throws ServletException, IOException {
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/test");
        when(request.getMethod()).thenReturn("GET");
        when(request.getQueryString()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent");
        doThrow(new IOException("Test exception")).when(filterChain).doFilter(any(), any());

        try {
            requestLoggingFilter.doFilterInternal(request, response, filterChain);
        } catch (IOException e) {
            // Expected exception
        }

        verify(response).setHeader(eq("X-Correlation-ID"), any(String.class));
    }
}
