package com.tarantulacommunity.tarantulasocial.security;

import com.tarantulacommunity.tarantulasocial.security.headers.SecurityHeadersFilter;
import com.tarantulacommunity.tarantulasocial.security.logging.RequestLoggingFilter;
import com.tarantulacommunity.tarantulasocial.security.auth.filter.JwtAuthFilter;
import com.tarantulacommunity.tarantulasocial.security.auth.service.CustomUserDetailsService;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter.IpRateLimitFilter;
import com.tarantulacommunity.tarantulasocial.security.features.rate_limit.filter.UserRateLimitFilter;
import com.tarantulacommunity.tarantulasocial.security.features.refresh_token.properties.CookieProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
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
    private final IpRateLimitFilter ipRateLimitFilter;
    private final UserRateLimitFilter userRateLimitFilter;
    private final RequestLoggingFilter requestLoggingFilter;
    private final SecurityHeadersFilter securityHeadersFilter;

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
    public FilterRegistrationBean<RequestLoggingFilter> registration(RequestLoggingFilter filter) {
        FilterRegistrationBean<RequestLoggingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Truly Stateless CSRF Repository that locks the cookie jar
        CsrfTokenRepository statelessCsrfRepository = new CsrfTokenRepository() {

            @Override
            public CsrfToken generateToken(HttpServletRequest request) {
                // Generates a cryptographically secure random token string for new sessions
                return new DefaultCsrfToken("X-XSRF-TOKEN", "_csrf", java.util.UUID.randomUUID().toString());
            }

            @Override
            public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
                // Check if the browser already sent an XSRF-TOKEN cookie.
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

        // Disable BREACH protection (XOR scrambling) so React reads the raw string cleanly
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
                        .requestMatchers("/api/v1/auth/resend-verification").authenticated()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/species/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/users/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/collection/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // Actuator endpoints - health and info public, metrics restricted to admins
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Swagger/OpenAPI documentation - admin only but permitAll() for production
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/webjars/**"
                        ).permitAll()

                        .anyRequest().hasAnyRole("USER", "ADMIN")
                )
                .authenticationProvider(authenticationProvider())
                // Security headers filter runs first (highest precedence via @Order)
                .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
                // Request logging filter runs second to capture all requests
                .addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class)
                // IP-based limiter runs third — before authentication even
                // resolves — so it's cheap to hit and caps abuse regardless of
                // whether the request carries a valid token.
                .addFilterBefore(ipRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // User-based limiter runs right after JwtAuthFilter, so
                // SecurityContextHolder already has the resolved principal
                // (or doesn't, for anonymous requests, which it skips).
                .addFilterAfter(userRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
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
                "Content-Type",
                "Authorization",
                "X-XSRF-TOKEN",
                "X-CSRF-TOKEN",
                "Accept",
                "Origin"
        ));

        config.setExposedHeaders(List.of(
                "X-CSRF-TOKEN",
                "X-XSRF-TOKEN",
                "X-RateLimit-Limit",
                "X-RateLimit-Remaining",
                "X-RateLimit-Reset",
                "Retry-After"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/swagger-ui/**", config);
        source.registerCorsConfiguration("/v3/api-docs/**", config);
        source.registerCorsConfiguration("/webjars/**", config);

        source.registerCorsConfiguration("/actuator/**", config);

        return source;
    }
}