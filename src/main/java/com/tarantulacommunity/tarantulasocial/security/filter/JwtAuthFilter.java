package com.tarantulacommunity.tarantulasocial.security.filter;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final String accessTokenCookieName;
    private final Long adminUserId;

    // Use clean constructor injection to map configuration properties
    public JwtAuthFilter(JwtUtil jwtUtil,
                         @Value("${app.cookie.access-token-name}") String accessTokenCookieName,
                         @Value("${app.admin.user-id:1}") Long adminUserId) { // Defaults to 1 if missing
        this.jwtUtil = jwtUtil;
        this.accessTokenCookieName = accessTokenCookieName;
        this.adminUserId = adminUserId;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            extractToken(request)
                    .filter(jwtUtil::isValid)
                    .ifPresent(token -> {
                        Long userId = jwtUtil.extractUserId(token);
                        String email = jwtUtil.extractEmail(token);

                        UserEntity userEntity = new UserEntity();
                        userEntity.setId(userId);
                        userEntity.setEmail(email);
                        userEntity.setPassword("[STATELESS]"); // Stubbed since JWT handles authentication

                        CustomUserDetails userDetails = new CustomUserDetails(userEntity, adminUserId);

                        UsernamePasswordAuthenticationToken auth =
                                UsernamePasswordAuthenticationToken.authenticated(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        auth.setDetails(new WebAuthenticationDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    });
        }

        chain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> accessTokenCookieName.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
