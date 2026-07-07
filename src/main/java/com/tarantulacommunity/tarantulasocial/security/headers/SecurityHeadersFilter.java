package com.tarantulacommunity.tarantulasocial.security.headers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Adds security headers to all HTTP responses for production hardening.
 */
@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Content Security Policy - Restrict resources that can be loaded
        // For API-only backend, we can be restrictive since we don't serve HTML
        response.setHeader("Content-Security-Policy",
                "default-src 'none'; " +
                "frame-ancestors 'none';");

        // Prevent clickjacking attacks
        response.setHeader("X-Frame-Options", "DENY");

        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Enable XSS protection (modern browsers have built-in protection)
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Referrer Policy - Control how much referrer information is sent
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Permissions Policy - Control browser features
        response.setHeader("Permissions-Policy",
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=()");

        // HSTS (HTTP Strict Transport Security) - Only in production with HTTPS
        if (isProduction()) {
            response.setHeader("Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains; preload");
        }

        // Remove server information for security through obscurity
        response.setHeader("Server", "");

        filterChain.doFilter(request, response);
    }

    private boolean isProduction() {
        return baseUrl != null && !baseUrl.contains("localhost") && !baseUrl.contains("127.0.0.1");
    }
}
