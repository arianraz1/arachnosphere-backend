package com.tarantulacommunity.tarantulasocial.security.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    private static final Pattern SANITIZE_PATTERN = Pattern.compile(
            "(?i)(\"password\"\\s*:\\s*\")([^\"]*)(\")|(password\\s*=\\s*)([^&\\s]*)"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator/health");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Generate or extract correlation ID
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add to MDC for logging
        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);

        // Add to response header for client tracking
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        // Wrap request and response for logging
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 0);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(wrappedRequest, wrappedResponse, duration, correlationId);
            wrappedResponse.copyBodyToResponse();
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request,
                                    ContentCachingResponseWrapper response,
                                    long duration,
                                    String correlationId) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = sanitizePayload(request.getQueryString()); // Scrub query string
            String fullUri = queryString != null ? uri + "?" + queryString : uri;
            int status = response.getStatus();
            String clientIp = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            log.info("Request: {} {} | Status: {} | Duration: {}ms | IP: {} | Correlation-ID: {} | User-Agent: {}",
                    method, fullUri, status, duration, clientIp, correlationId, userAgent);

            // Log request body for non-GET requests (if small enough)
            if (!"GET".equals(method) && request.getContentAsByteArray().length > 0) {
                String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (requestBody.length() < 1000) {
                    String cleanBody = sanitizePayload(requestBody); // Scrub request body
                    log.debug("Request Body: {}", cleanBody);
                }
            }

            // Log response body for errors (if small enough)
            if (status >= 400 && response.getContentAsByteArray().length > 0) {
                String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (responseBody.length() < 1000) {
                    log.debug("Response Body: {}", responseBody);
                }
            }
        } catch (Exception e) {
            log.error("Error logging request/response", e);
        }
    }

    private String sanitizePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return payload;
        }

        Matcher matcher = SANITIZE_PATTERN.matcher(payload);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // JSON: "password":"secret"
                matcher.appendReplacement(sb, matcher.group(1) + "[REDACTED]" + matcher.group(3));
            } else {
                // Form/query: password=secret
                matcher.appendReplacement(sb, matcher.group(4) + "[REDACTED]");
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
