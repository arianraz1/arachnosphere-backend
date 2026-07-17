package com.tarantulacommunity.tarantulasocial.security.features.email.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URI;
import java.util.Set;

@Component
@ConditionalOnProperty(prefix = "security.email-verification", name = "enabled", havingValue = "true", matchIfMissing = true)
public class EmailVerificationFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    @Value("${app.base-url:http://localhost:8080/api/v1}")
    private String appBaseUrl;

    private String basePath() {
        try {
            String path = URI.create(appBaseUrl).getPath();
            return (path == null || path.isBlank()) ? "/api/v1" : path;
        } catch (Exception e) {
            return "/api/v1";
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        String base = basePath();

        if (!MUTATING_METHODS.contains(request.getMethod())
                || uri.startsWith(base + "/auth/")
                || uri.startsWith(base + "/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            filterChain.doFilter(request, response); // Spring Security's own checks handle unauthenticated
            return;
        }

        Boolean verified = (Boolean) request.getAttribute("emailVerified");
        if (verified == null || !verified) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Email verification required to perform this action.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}