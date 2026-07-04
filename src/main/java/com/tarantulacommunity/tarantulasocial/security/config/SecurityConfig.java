package com.tarantulacommunity.tarantulasocial.security.config;

import com.tarantulacommunity.tarantulasocial.security.filter.JwtAuthFilter;
import com.tarantulacommunity.tarantulasocial.security.service.CustomUserDetailsService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CookieProperties cookieProps;

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 1. RESEARCHED FIX: Truly Stateless CSRF Repository that locks the cookie jar
        CsrfTokenRepository statelessCsrfRepository = new CsrfTokenRepository() {

            @Override
            public CsrfToken generateToken(HttpServletRequest request) {
                // Generates a cryptographically secure random token string for new sessions
                return new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", java.util.UUID.randomUUID().toString());
            }

            @Override
            public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
                // CRITICAL: Check if the browser already sent an XSRF-TOKEN cookie.
                // If the cookie is already there, RETURN IMMEDIATELY. This breaks the endless refresh loop!
                if (request.getCookies() != null) {
                    boolean hasCookie = Arrays.stream(request.getCookies())
                            .anyMatch(c -> "XSRF-TOKEN".equals(c.getName()));
                    if (hasCookie) {
                        return; // Cookie jar is locked. Do not write another Set-Cookie header.
                    }
                }

                // If the user doesn't have the cookie yet (initial page load / login), drop it once
                if (token != null) {
                    Cookie cookie = new Cookie("XSRF-TOKEN", token.getToken());
                    cookie.setPath("/");
                    cookie.setHttpOnly(false); // Must be false so React can read it
                    cookie.setSecure(cookieProps.secure());
                    cookie.setAttribute("SameSite", "Lax");
                    if (cookieProps.domain() != null && !cookieProps.domain().isBlank()) {
                        cookie.setDomain(cookieProps.domain());
                    }
                    response.addCookie(cookie);
                }
            }

            @Override
            public CsrfToken loadToken(HttpServletRequest request) {
                if (request.getCookies() == null) return null;
                // Reads the existing cookie from the browser and converts it into a Spring CsrfToken object
                return Arrays.stream(request.getCookies())
                        .filter(c -> "XSRF-TOKEN".equals(c.getName()))
                        .map(c -> new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", c.getValue()))
                        .findFirst()
                        .orElse(null);
            }
        };

        // 2. Disable BREACH protection (XOR scrambling) so React reads the raw string cleanly
        CsrfTokenRequestAttributeHandler stableHandler = new CsrfTokenRequestAttributeHandler();
        stableHandler.setCsrfRequestAttributeName(null);

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf
                        .csrfTokenRepository(statelessCsrfRepository) // Injects our fixed repository
                        .csrfTokenRequestHandler(stableHandler)
                )
                .sessionManagement(s -> s
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .sessionAuthenticationStrategy(sessionAuthenticationStrategy())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/resend-verification").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/species/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/collection/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized"))
                        .accessDeniedHandler((req, res, e) -> res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden"))
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigin));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Content-Type", "Authorization", "X-XSRF-TOKEN", "Accept", "Origin"
        ));
        config.setExposedHeaders(List.of("X-CSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
