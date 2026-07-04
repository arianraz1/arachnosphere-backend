package com.tarantulacommunity.tarantulasocial.security.filter;

import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.config.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final String accessTokenCookieName;
    private final Long adminUserId;

    public JwtAuthFilter(JwtUtil jwtUtil,
                         UserRepository userRepository,
                         @Value("${app.cookie.access-token-name}") String accessTokenCookieName,
                         @Value("${app.admin.user-id:1}") Long adminUserId) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
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

                        userRepository.findById(userId).ifPresent(user -> {
                            CustomUserDetails userDetails = new CustomUserDetails(user, adminUserId);

                            List<GrantedAuthority> authorities = user.isEmailVerified()
                                    ? new ArrayList<>(userDetails.getAuthorities())
                                    : List.of();

                            UsernamePasswordAuthenticationToken auth =
                                    UsernamePasswordAuthenticationToken.authenticated(
                                            userDetails,
                                            null,
                                            authorities
                                    );

                            auth.setDetails(new WebAuthenticationDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(auth);
                        });
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