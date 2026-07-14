package com.tarantulacommunity.tarantulasocial.security.features.password.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.entity.PasswordResetTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.features.email.service.EmailService;
import com.tarantulacommunity.tarantulasocial.security.features.password.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    @Mock private PasswordResetTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(passwordResetService, "frontendUrl", "http://localhost:3000");
    }

    private UserEntity verifiedUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("testuser");
        user.setEmailVerified(true);
        return user;
    }

    private PasswordResetTokenEntity validToken(UserEntity user) {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setToken("valid-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));
        return token;
    }

    // --- sendResetEmail ---

    @Test
    void sendResetEmail_sendsEmailWhenUserExists() {
        UserEntity user = verifiedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@example.com");

        verify(emailService).sendPasswordResetEmail(eq("user@example.com"), eq("testuser"), anyString());
        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
    }

    @Test
    void sendResetEmail_doesNothingWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        passwordResetService.sendResetEmail("unknown@example.com");

        verifyNoInteractions(emailService, tokenRepository);
    }

    @Test
    void sendResetEmail_normalizesEmail() {
        UserEntity user = verifiedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("USER@EXAMPLE.COM");

        verify(userRepository).findByEmail("user@example.com");
    }

    @Test
    void sendResetEmail_deletesExistingTokenBeforeCreatingNew() {
        UserEntity user = verifiedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@example.com");

        InOrder order = inOrder(tokenRepository);
        order.verify(tokenRepository).deleteByUserId(1L);
        order.verify(tokenRepository).save(any());
    }

    @Test
    void sendResetEmail_resetLinkPointsToFrontend() {
        UserEntity user = verifiedUser();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@example.com");

        ArgumentCaptor<String> linkCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendPasswordResetEmail(any(), any(), linkCaptor.capture());
        assertThat(linkCaptor.getValue()).startsWith("http://localhost:3000/reset-password?token=");
    }

    // --- resetPassword ---

    @Test
    void resetPassword_updatesPasswordWhenTokenValid() {
        UserEntity user = verifiedUser();
        PasswordResetTokenEntity token = validToken(user);

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode("newpassword")).thenReturn("hashed");

        passwordResetService.resetPassword("valid-token", "newpassword");

        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        assertThat(user.getPassword()).isEqualTo("hashed");
    }

    @Test
    void resetPassword_throwsWhenTokenNotFound() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.InvalidTokenException.class,
                () -> passwordResetService.resetPassword("invalid", "newpassword"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void resetPassword_throwsWhenTokenExpired() {
        UserEntity user = verifiedUser();
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setToken("expired-token");
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThrows(GlobalExceptionHandler.InvalidTokenException.class,
                () -> passwordResetService.resetPassword("expired-token", "newpassword"));

        verify(tokenRepository).delete(token);
        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void resetPassword_deletesTokenAfterSuccessfulReset() {
        UserEntity user = verifiedUser();
        PasswordResetTokenEntity token = validToken(user);

        when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
        when(passwordEncoder.encode(any())).thenReturn("hashed");

        passwordResetService.resetPassword("valid-token", "newpassword");

        verify(tokenRepository).delete(token);
    }

    @Test
    void sendResetEmail_throwsTooManyRequestsWhenCooldownNotExpired() {
        UserEntity user = verifiedUser();
        PasswordResetTokenEntity existingToken = new PasswordResetTokenEntity();
        existingToken.setUser(user);
        existingToken.setToken("existing-token");
        existingToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        existingToken.setCreatedAt(LocalDateTime.now().minusSeconds(30)); // 30s ago, cooldown is 60s

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserId(1L)).thenReturn(Optional.of(existingToken));

        assertThrows(GlobalExceptionHandler.TooManyRequestsException.class,
                () -> passwordResetService.sendResetEmail("user@example.com"));

        verifyNoInteractions(emailService);
        verify(tokenRepository, never()).deleteByUserId(any());
    }

    @Test
    void sendResetEmail_allowsResendWhenCooldownExpired() {
        UserEntity user = verifiedUser();
        PasswordResetTokenEntity existingToken = new PasswordResetTokenEntity();
        existingToken.setUser(user);
        existingToken.setToken("existing-token");
        existingToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        existingToken.setCreatedAt(LocalDateTime.now().minusSeconds(301)); // 61s ago, cooldown expired

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserId(1L)).thenReturn(Optional.of(existingToken));
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@example.com");

        verify(emailService).sendPasswordResetEmail(any(), any(), any());
        verify(tokenRepository).deleteByUserId(1L);
    }

    @Test
    void sendResetEmail_allowsResendWhenNoPreviousToken() {
        UserEntity user = verifiedUser();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(tokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        passwordResetService.sendResetEmail("user@example.com");

        verify(emailService).sendPasswordResetEmail(any(), any(), any());
    }
}
