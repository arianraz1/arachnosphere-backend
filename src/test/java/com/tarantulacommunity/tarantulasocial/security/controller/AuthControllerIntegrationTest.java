package com.tarantulacommunity.tarantulasocial.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarantulacommunity.tarantulasocial.TarantulaProjectApplication;
import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.security.entity.EmailVerificationTokenEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.repository.EmailVerificationTokenRepository;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.service.RefreshTokenService;
import com.tarantulacommunity.tarantulasocial.security.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(classes = TarantulaProjectApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private EmailVerificationTokenRepository emailVerificationTokenRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---------- NEW CSRF ENDPOINT TEST ----------
    @Test
    void shouldGetCsrfTokenAndSetCookie() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String setCookieHeader = result.getResponse().getHeader("Set-Cookie");
        assert setCookieHeader != null && setCookieHeader.contains("XSRF-TOKEN");
    }

    // ---------- LOGIN ----------
    @Test
    void shouldLoginAndReceiveCookiesAndCsrfToken() throws Exception {
        createUser("login@test.com", "loginuser", "pass123");

        LoginRequest request = new LoginRequest("login@test.com", "pass123");
        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()));

        result.andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(jsonPath("$.user.id").exists())
                .andExpect(jsonPath("$.user.email").value("login@test.com"))
                .andExpect(jsonPath("$.csrfToken").exists());
    }

    // ---------- REFRESH (now returns 200 + csrfToken) ----------
    @Test
    void shouldRefreshTokenAndGetNewCookiesAndCsrfToken() throws Exception {
        UserEntity user = createUser("refresh@test.com", "refresh", "pass");
        String rawRefresh = refreshTokenService.issue(user);

        ResultActions result = mockMvc.perform(post("/api/auth/refresh")
                .cookie(buildRefreshCookie(rawRefresh))
                .with(csrf()));

        result.andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(jsonPath("$.csrfToken").exists());
    }

    // ---------- LOGOUT ----------
    @Test
    void shouldLogoutAndClearCookies() throws Exception {
        UserEntity user = createUser("logout@test.com", "logout", "pass");
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        ResultActions result = mockMvc.perform(post("/api/auth/logout")
                .cookie(buildAccessCookie(accessToken))
                .with(csrf()));

        result.andExpect(status().isNoContent())
                .andExpect(cookie().value("access_token", ""))
                .andExpect(cookie().value("refresh_token", ""));
    }

    // ---------- LOGIN — UNVERIFIED EMAIL ----------
    @Test
    void shouldAllowLoginWhenEmailNotVerified() throws Exception {
        createUser("unverified@test.com", "unverified", "pass123", false);

        LoginRequest request = new LoginRequest("unverified@test.com", "pass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn403WhenUnverifiedUserAccessesProtectedEndpoint() throws Exception {
        createUser("unverified@test.com", "unverified", "pass123", false);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("unverified@test.com", "pass123")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = loginResult.getResponse().getCookie("access_token").getValue();

        mockMvc.perform(get("/api/messages/inbox")
                        .cookie(new Cookie("access_token", accessToken))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ---------- VERIFY EMAIL ----------
    @Test
    void shouldVerifyEmailWithValidToken() throws Exception {
        UserEntity user = createUser("verify@test.com", "verifyuser", "pass123", false);

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("valid-test-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        emailVerificationTokenRepository.save(token);

        mockMvc.perform(get("/api/auth/verify-email").param("token", "valid-test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());

        UserEntity refreshed = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(refreshed.isEmailVerified());
    }

    @Test
    void shouldRejectVerifyEmailWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/verify-email").param("token", "nonexistent-token"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectVerifyEmailWithExpiredToken() throws Exception {
        UserEntity user = createUser("expired@test.com", "expireduser", "pass123", false);

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("expired-test-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        emailVerificationTokenRepository.save(token);

        mockMvc.perform(get("/api/auth/verify-email").param("token", "expired-test-token"))
                .andExpect(status().isBadRequest());
    }

    // ---------- RESEND VERIFICATION ----------
    @Test
    void shouldReturnEmailVerifiedFalseInLoginResponseWhenUnverified() throws Exception {
        createUser("unverified2@test.com", "unverified2", "pass123", false);

        LoginRequest request = new LoginRequest("unverified2@test.com", "pass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(false));
    }

    @Test
    void shouldReturnEmailVerifiedTrueInLoginResponseWhenVerified() throws Exception {
        createUser("verified@test.com", "verified", "pass123", true);

        LoginRequest request = new LoginRequest("verified@test.com", "pass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.emailVerified").value(true));
    }

    // ---------- CASE INSENSITIVE LOGIN ----------
    @Test
    void shouldLoginWithMixedCaseEmail() throws Exception {
        createUser("login2@test.com", "loginuser2", "pass123");

        LoginRequest request = new LoginRequest("LOGIN2@TEST.COM", "pass123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.email").value("login2@test.com"));
    }

    // ---------- DUPLICATE EMAIL CASE INSENSITIVE ----------
    @Test
    void shouldNotRegisterDuplicateEmailWithDifferentCase() throws Exception {
        createUser("duplicate@test.com", "user1", "pass1234");

        UserRegisterRequest request = new UserRegisterRequest("DUPLICATE@TEST.COM", "user2", "pass1234");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    // ---------- RESEND VERIFICATION ----------
    @Test
    void shouldResendVerificationEmailWhenAuthenticated() throws Exception {
        UserEntity user = createUser("resend@test.com", "resenduser", "pass123", false);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());

        mockMvc.perform(post("/api/auth/resend-verification")
                        .cookie(buildAccessCookie(accessToken))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn401ForResendVerificationWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ---------- HELPERS ----------
    private Cookie buildRefreshCookie(String value) {
        Cookie c = new Cookie("refresh_token", value);
        c.setPath("/api/auth/refresh");
        c.setDomain("localhost");
        c.setHttpOnly(true);
        c.setSecure(false);
        return c;
    }

    private Cookie buildAccessCookie(String value) {
        Cookie c = new Cookie("access_token", value);
        c.setPath("/");
        c.setDomain("localhost");
        c.setHttpOnly(true);
        c.setSecure(false);
        return c;
    }

    private UserEntity createUser(String email, String username, String rawPassword) {
        return createUser(email, username, rawPassword, true);
    }

    private UserEntity createUser(String email, String username, String rawPassword, boolean emailVerified) {
        UserEntity user = emailVerified
                ? TestUtilities.verifiedUser(email, username)
                : TestUtilities.unverifiedUser(email, username);
        user.setEmail(email.toLowerCase().trim());
        user.setUsername(username.toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }
}
