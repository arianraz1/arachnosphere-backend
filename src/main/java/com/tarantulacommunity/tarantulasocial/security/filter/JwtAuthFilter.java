package com.tarantulacommunity.tarantulasocial.security.filter;

import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String accessTokenCookieName;

    // Spring will inject both parameters – the first from the bean container, the second via @Value
    public JwtAuthFilter(JwtUtil jwtUtil,
                         @Value("${app.cookie.access-token-name}") String accessTokenCookieName) {
        this.jwtUtil = jwtUtil;
        this.accessTokenCookieName = accessTokenCookieName;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        extractToken(request)
                .filter(jwtUtil::isValid)
                .ifPresent(token -> {
                    Long userId = jwtUtil.extractUserId(token);
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, List.of());
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                });

        chain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return Optional.empty();
        return Arrays.stream(request.getCookies())
                .filter(c -> accessTokenCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}