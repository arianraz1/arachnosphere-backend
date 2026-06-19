package com.tarantulacommunity.tarantulasocial.security.service;

import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginResponse;
import com.tarantulacommunity.tarantulasocial.security.mapper.AuthMapper;
import com.tarantulacommunity.tarantulasocial.security.util.CookieUtil;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final AuthMapper authMapper;

    public AuthResult register(UserRegisterRequest request) {
        UserSignupResponse signupResponse = userService.registerUser(request);

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after registration"));

        TokenPair tokens = issueTokens(user);
        return new AuthResult(signupResponse, tokens);
    }

    public AuthResult login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("Unexpected principal type");
        }
        UserEntity user = userDetails.getUser();
        TokenPair tokens = issueTokens(user);
        LoginResponse loginResponse = authMapper.toLoginResponse(user);
        return new AuthResult(loginResponse, tokens);
    }

    public TokenPair refresh(String rawRefreshToken) {
        RefreshTokenService.RotationResult result = refreshTokenService.rotate(rawRefreshToken);
        UserEntity user = result.user();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshRaw = result.newRawToken();

        return new TokenPair(
                cookieUtil.accessTokenCookie(newAccessToken).toString(),
                cookieUtil.refreshTokenCookie(newRefreshRaw).toString()
        );
    }

    public void logout(Long userId) {
        if (userId != null) {
            userRepository.findById(userId).ifPresent(refreshTokenService::revokeAll);
        }
    }

    public TokenPair clearCookies() {
        return new TokenPair(
                cookieUtil.clearAccessToken().toString(),
                cookieUtil.clearRefreshToken().toString()
        );
    }

    private TokenPair issueTokens(UserEntity user) {
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = refreshTokenService.issue(user);
        return new TokenPair(
                cookieUtil.accessTokenCookie(accessToken).toString(),
                cookieUtil.refreshTokenCookie(refreshToken).toString()
        );
    }

    public record AuthResult(Object responseBody, TokenPair tokens) {}
    public record TokenPair(String accessTokenCookie, String refreshTokenCookie) {}
}