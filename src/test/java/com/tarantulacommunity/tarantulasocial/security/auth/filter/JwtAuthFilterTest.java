package com.tarantulacommunity.tarantulasocial.security.auth.filter;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    private JwtAuthFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @Mock
    private FilterChain chain;

    private UserEntity validUser;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtUtil, userRepository, "access_token", 1L);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();

        validUser = new UserEntity();
        validUser.setId(5L);
        validUser.setEmail("user@example.com");
        validUser.setPassword("hashed");
        validUser.setEmailVerified(true);
    }

    @Test
    void shouldSetAuthenticationForValidToken() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(validUser));

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
    void shouldNotAuthenticateWhenUserNotFoundInDatabase() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(99L);
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
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

    @Test
    void shouldNotAuthenticateWhenAuthenticationAlreadyExists() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "existing", null, Collections.emptyList()
                )
        );

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(String.class);
    }

    @Test
    void shouldNotAuthenticateWithWrongCookieName() throws Exception {
        request.setCookies(new Cookie("wrong_cookie", "valid.jwt.token"));

        filter.doFilterInternal(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldAuthenticateWithMultipleCookiesWhenCorrectOnePresent() throws Exception {
        Cookie wrongCookie = new Cookie("wrong_cookie", "token");
        Cookie rightCookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(wrongCookie, rightCookie);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(validUser));

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isInstanceOf(CustomUserDetails.class);
    }

    @Test
    void shouldNotAuthenticateWithNullCookies() throws Exception {
        request.setCookies((Cookie[]) null);

        filter.doFilterInternal(request, response, chain);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void shouldSetEmptyAuthoritiesWhenEmailNotVerified() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        validUser.setEmailVerified(false);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(validUser));

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    void shouldSetFullAuthoritiesWhenEmailVerified() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(validUser));

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isNotEmpty();
    }

    @Test
    void shouldGrantAdminRoleToAdminUser() throws Exception {
        Cookie cookie = new Cookie("access_token", "valid.jwt.token");
        request.setCookies(cookie);

        validUser.setId(1L); // admin user id

        given(jwtUtil.isValid("valid.jwt.token")).willReturn(true);
        given(jwtUtil.extractUserId("valid.jwt.token")).willReturn(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(validUser));

        filter.doFilterInternal(request, response, chain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
}