package com.tarantulacommunity.tarantulasocial.security.controller;

import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request,
                                      HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        setCookies(response, result.tokens());
        return ResponseEntity.status(HttpStatus.CREATED).body(result.responseBody());
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        setCookies(response, result.tokens());
        return ResponseEntity.ok(result.responseBody());
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "${app.cookie.refresh-token-name}", required = false)
            String rawRefreshToken,
            HttpServletResponse response) {
        if (rawRefreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthService.TokenPair tokens = authService.refresh(rawRefreshToken);
        setCookies(response, tokens);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Long userId,
                                       HttpServletResponse response) {
        authService.logout(userId);
        AuthService.TokenPair clearCookies = authService.clearCookies();
        setCookies(response, clearCookies);
        return ResponseEntity.noContent().build();
    }

    private void setCookies(HttpServletResponse response, AuthService.TokenPair tokens) {
        response.addHeader(HttpHeaders.SET_COOKIE, tokens.accessTokenCookie());
        response.addHeader(HttpHeaders.SET_COOKIE, tokens.refreshTokenCookie());
    }
}