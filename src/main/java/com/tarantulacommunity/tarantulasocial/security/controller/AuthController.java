package com.tarantulacommunity.tarantulasocial.security.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest;
import com.tarantulacommunity.tarantulasocial.security.service.EmailVerificationService;
import com.tarantulacommunity.tarantulasocial.security.config.CookieProperties;
import com.tarantulacommunity.tarantulasocial.security.config.CustomUserDetails;
import com.tarantulacommunity.tarantulasocial.security.dto.request.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.service.AuthService;
import com.tarantulacommunity.tarantulasocial.security.service.PasswordResetService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CookieProperties cookieProps;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;
    private final PasswordResetService passwordResetService;

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    @Value("${app.cookie.refresh-token-name}")
    private String refreshTokenName;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken csrfToken) {
        return csrfToken;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegisterRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        setCookies(response, result.tokens());

        String newCsrf = rotateCsrfToken(response);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("user", result.responseBody());
        responseBody.put("csrfToken", newCsrf);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseBody);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        setCookies(response, result.tokens());

        String newCsrf = rotateCsrfToken(response);

        Map<String, Object> responseBody = new LinkedHashMap<>();
        responseBody.put("user", result.responseBody());
        responseBody.put("csrfToken", newCsrf);

        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token, HttpServletRequest request) {
        emailVerificationService.verifyToken(token);

        String acceptHeader = request.getHeader(HttpHeaders.ACCEPT);
        if (acceptHeader != null && acceptHeader.contains("text/html")) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/verified"))
                    .build();
        }

        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userRepository.findById(userDetails.getId()).ifPresent(user -> {
            if (!user.isEmailVerified()) {
                try {
                    emailVerificationService.sendVerificationEmail(user);
                } catch (GlobalExceptionHandler.TooManyRequestsException ex) {
                    log.debug("Resend verification rate-limited for user id {}", user.getId());
                }
            }
        });

        return ResponseEntity.ok(Map.of(
                "message", "If your email is not yet verified, a verification link has been sent."
        ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = null;
        if (request.getCookies() != null) {
            rawRefreshToken = Arrays.stream(request.getCookies())
                    .filter(cookie -> refreshTokenName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElse(null);
        }

        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        AuthService.TokenPair tokens = authService.refresh(rawRefreshToken);
        setCookies(response, tokens);

        String newCsrf = rotateCsrfToken(response);

        return ResponseEntity.ok(Map.of("csrfToken", newCsrf));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails, HttpServletResponse response) {
        if (userDetails != null) {
            authService.logout(userDetails.getId());
        }

        AuthService.TokenPair clearCookies = authService.clearCookies();
        setCookies(response, clearCookies);

        ResponseCookie clearCsrf = ResponseCookie.from("XSRF-TOKEN", "")
                .path("/")
                .maxAge(0)
                .secure(cookieProps.secure())
                .sameSite("Lax")
                .build();

        if (cookieProps.domain() != null && !cookieProps.domain().isBlank()) {
            clearCsrf = ResponseCookie.from("XSRF-TOKEN", "")
                    .path("/")
                    .maxAge(0)
                    .domain(cookieProps.domain())
                    .secure(cookieProps.secure())
                    .sameSite("Lax")
                    .build();
        }

        response.addHeader(HttpHeaders.SET_COOKIE, clearCsrf.toString());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        passwordResetService.sendResetEmail(email);
        return ResponseEntity.ok(Map.of(
                "message", "If that email is registered, a password reset link has been sent."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully. You can now log in."));
    }

    private void setCookies(HttpServletResponse response, AuthService.TokenPair tokens) {
        response.addHeader(HttpHeaders.SET_COOKIE, tokens.accessTokenCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, tokens.refreshTokenCookie().toString());
    }

    /**
     * Helper method to generate and write a completely fresh, authenticated CSRF session token.
     * This bypasses the custom repository freeze guard via explicit response line insertion.
     */
    private String rotateCsrfToken(HttpServletResponse response) {
        String freshTokenValue = UUID.randomUUID().toString();

        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("XSRF-TOKEN", freshTokenValue)
                .path("/")
                .httpOnly(false) // JavaScript readable for React Axios extraction mapping
                .secure(cookieProps.secure())
                .sameSite("Lax");

        if (cookieProps.domain() != null && !cookieProps.domain().isBlank()) {
            cookieBuilder.domain(cookieProps.domain());
        }

        response.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
        return freshTokenValue;
    }
}