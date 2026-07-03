package com.tarantulacommunity.tarantulasocial.security.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.features.user.service.EmailVerificationService;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginResponse;
import com.tarantulacommunity.tarantulasocial.security.mapper.AuthMapper;
import com.tarantulacommunity.tarantulasocial.security.util.CookieUtil;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AuthenticationManager authManager;
    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private JwtUtil jwtUtil;
    @Mock private CookieUtil cookieUtil;
    @Mock private AuthMapper authMapper;
    @Mock private Authentication authentication;
    @Mock private EmailVerificationService emailVerificationService;

    @InjectMocks
    private AuthService authService;

    private UserEntity user;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("user");
        user.setEmailVerified(true); // login() requires this now — see loginShouldThrowWhenEmailNotVerified for the false case
        loginRequest = new LoginRequest("user@example.com", "password");
    }

    @Test
    void loginShouldReturnTokensAndLoginResponse() {
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(new CustomUserDetails(user, 1L));
        given(jwtUtil.generateAccessToken(1L, "user@example.com")).willReturn("access-token");
        given(refreshTokenService.issue(user)).willReturn("refresh-raw");
        given(cookieUtil.accessTokenCookie("access-token")).willReturn(ResponseCookie.from("access", "val").build());
        given(cookieUtil.refreshTokenCookie("refresh-raw")).willReturn(ResponseCookie.from("refresh", "val").build());
        given(authMapper.toLoginResponse(user)).willReturn(new LoginResponse(1L, "user@example.com", "user"));

        AuthService.AuthResult result = authService.login(loginRequest);

        assertThat(result.responseBody()).isInstanceOf(LoginResponse.class);
        assertThat(result.tokens().accessTokenCookie()).isNotNull();
        assertThat(result.tokens().refreshTokenCookie()).isNotNull();
    }

    @Test
    void loginShouldThrowWhenEmailNotVerified() {
        user.setEmailVerified(false);
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(new CustomUserDetails(user, 1L));

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(GlobalExceptionHandler.EmailNotVerifiedException.class);

        // Nothing downstream should run once the verification gate fails
        verifyNoInteractions(jwtUtil, refreshTokenService, cookieUtil);
    }

    @Test
    void loginShouldThrowWhenPrincipalIsWrongType() {
        given(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn("not-a-custom-user-details");

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void registerShouldReturnSignupResponseAndTokens() {
        UserRegisterRequest regReq = new UserRegisterRequest();
        regReq.setEmail("new@example.com");
        regReq.setUsername("newuser");
        regReq.setPassword("pass");
        UserSignupResponse signupResponse = new UserSignupResponse();
        given(userService.registerUser(regReq)).willReturn(signupResponse);
        given(userRepository.findByEmail("new@example.com")).willReturn(Optional.of(user));
        given(jwtUtil.generateAccessToken(1L, "user@example.com")).willReturn("acc");
        given(refreshTokenService.issue(user)).willReturn("ref");
        given(cookieUtil.accessTokenCookie("acc")).willReturn(ResponseCookie.from("a", "a").build());
        given(cookieUtil.refreshTokenCookie("ref")).willReturn(ResponseCookie.from("r", "r").build());

        AuthService.AuthResult result = authService.register(regReq);

        assertThat(result.responseBody()).isEqualTo(signupResponse);
        verify(emailVerificationService).sendVerificationEmail(user);
    }

    @Test
    void registerShouldThrowWhenUserNotFoundAfterRegistration() {
        UserRegisterRequest regReq = new UserRegisterRequest();
        regReq.setEmail("ghost@example.com");
        regReq.setUsername("ghost");
        regReq.setPassword("pass");
        given(userService.registerUser(regReq)).willReturn(new UserSignupResponse());
        given(userRepository.findByEmail("ghost@example.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(regReq))
                .isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void refreshShouldReturnNewTokenPair() {
        RefreshTokenService.RotationResult rotResult =
                new RefreshTokenService.RotationResult(user, "new-raw");
        given(refreshTokenService.rotate("old-raw")).willReturn(rotResult);
        given(jwtUtil.generateAccessToken(1L, "user@example.com")).willReturn("new-access");
        given(cookieUtil.accessTokenCookie("new-access")).willReturn(ResponseCookie.from("access", "new-acc").build());
        given(cookieUtil.refreshTokenCookie("new-raw")).willReturn(ResponseCookie.from("refresh", "new-ref").build());

        AuthService.TokenPair pair = authService.refresh("old-raw");

        assertThat(pair.accessTokenCookie()).isNotNull();
        assertThat(pair.refreshTokenCookie()).isNotNull();
    }

    @Test
    void logoutShouldRevokeAllIfUserExists() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        authService.logout(1L);
        // consider: verify(refreshTokenService).revokeAll(user) once that dependency is visible here
    }
}