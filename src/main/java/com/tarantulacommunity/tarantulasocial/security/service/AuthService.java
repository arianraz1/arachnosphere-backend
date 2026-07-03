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
import com.tarantulacommunity.tarantulasocial.security.mapper.AuthMapper;
import com.tarantulacommunity.tarantulasocial.security.util.CookieUtil;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final AuthMapper authMapper;
    private final EmailVerificationService emailVerificationService;

    @Transactional
    public AuthResult register(UserRegisterRequest request) {
        UserSignupResponse signupResponse = userService.registerUser(request);

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after registration"));

        emailVerificationService.sendVerificationEmail(user);

        TokenPair tokens = issueTokens(user);
        return new AuthResult(signupResponse, tokens);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BadCredentialsException("Invalid user principal state");
        }

        UserEntity user = userDetails.getUser();

        if (!user.isEmailVerified()) {
            throw new GlobalExceptionHandler.EmailNotVerifiedException("Please verify your email before logging in");
        }

        TokenPair tokens = issueTokens(user);
        var loginResponse = authMapper.toLoginResponse(user);

        return new AuthResult(loginResponse, tokens);
    }

    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawRefreshToken);
        UserEntity user = result.user();

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshRaw = result.newRawToken();

        return new TokenPair(
                cookieUtil.accessTokenCookie(newAccessToken),
                cookieUtil.refreshTokenCookie(newRefreshRaw)
        );
    }

    @Transactional
    public void logout(Long userId) {
        userRepository.findById(userId).ifPresent(refreshTokenService::revokeAll);
    }

    public TokenPair clearCookies() {
        return new TokenPair(
                cookieUtil.clearAccessToken(),
                cookieUtil.clearRefreshToken()
        );
    }

    private TokenPair issueTokens(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.issue(user);

        return new TokenPair(
                cookieUtil.accessTokenCookie(accessToken),
                cookieUtil.refreshTokenCookie(refreshToken)
        );
    }

    public record AuthResult(Object responseBody, TokenPair tokens) {}
    public record TokenPair(ResponseCookie accessTokenCookie, ResponseCookie refreshTokenCookie) {}
}