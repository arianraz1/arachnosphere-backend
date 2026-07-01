package com.tarantulacommunity.tarantulasocial.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarantulacommunity.tarantulasocial.TarantulaProjectApplication;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
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

    // ---------- HELPERS ----------
    private UserEntity createUser(String email, String username, String rawPassword) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        return userRepository.save(user);
    }

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
}