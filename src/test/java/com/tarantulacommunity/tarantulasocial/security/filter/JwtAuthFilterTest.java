package com.tarantulacommunity.tarantulasocial.security.filter;

import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    private JwtAuthFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        // Manually create the filter with the mock and a known cookie name
        filter = new JwtAuthFilter(jwtUtil, "access_token", 1L);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSetAuthenticationForValidToken() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(5L);
        given(jwtUtil.extractEmail("valid.jwt.token")).willReturn("user@example.com");

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();

        assertThat(auth.getPrincipal()).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails principal = (CustomUserDetails) auth.getPrincipal();
        assertThat(principal.getId()).isEqualTo(5L);
        assertThat(principal.getUsername()).isEqualTo("user@example.com");

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWithoutCookie() throws Exception {
        filter.doFilterInternal(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldNotAuthenticateWithInvalidToken() throws Exception {
        request.setCookies(new Cookie("access_token", "bad"));
        given(jwtUtil.isValid("bad")).willReturn(false);

        filter.doFilterInternal(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}