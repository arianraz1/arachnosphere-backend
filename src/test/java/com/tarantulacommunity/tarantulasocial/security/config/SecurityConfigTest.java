package com.tarantulacommunity.tarantulasocial.security.config;

import com.tarantulacommunity.tarantulasocial.security.filter.JwtAuthFilter;
import com.tarantulacommunity.tarantulasocial.security.service.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CookieProperties cookieProps;

    @Test
    void passwordEncoderBean_shouldReturnBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        
        assertThat(passwordEncoder).isNotNull();
        String encoded = passwordEncoder.encode("testPassword");
        assertThat(encoded).isNotEqualTo("testPassword");
        assertThat(passwordEncoder.matches("testPassword", encoded)).isTrue();
    }

    @Test
    void authenticationProviderBean_shouldReturnDaoAuthenticationProvider() {
        DaoAuthenticationProvider provider = securityConfig.authenticationProvider();
        
        assertThat(provider).isNotNull();
    }

    @Test
    void corsConfigurationSourceBean_shouldReturnValidConfiguration() {
        CorsConfigurationSource corsSource = securityConfig.corsConfigurationSource();
        
        assertThat(corsSource).isNotNull();
    }

    @Test
    void sessionAuthenticationStrategyBean_shouldReturnNullAuthenticatedSessionStrategy() {
        var strategy = securityConfig.sessionAuthenticationStrategy();
        
        assertThat(strategy).isNotNull();
    }
}
