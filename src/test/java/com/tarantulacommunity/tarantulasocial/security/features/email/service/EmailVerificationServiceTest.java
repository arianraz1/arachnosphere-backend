package com.tarantulacommunity.tarantulasocial.security.features.email.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.entity.EmailVerificationTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.features.email.event.DeletionWarningEmailRequestedEvent;
import com.tarantulacommunity.tarantulasocial.security.features.email.event.VerificationEmailRequestedEvent;
import com.tarantulacommunity.tarantulasocial.security.features.email.repository.EmailVerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private UserRepository userRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private EmailVerificationService emailVerificationService;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailVerificationService, "baseUrl", "http://localhost:8080");
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setUsername("user");
    }

    @Test
    void sendVerificationEmail_noExistingToken_savesTokenAndPublishesEvent() {
        given(tokenRepository.findByUserId(1L)).willReturn(Optional.empty());

        emailVerificationService.sendVerificationEmail(user);

        verify(tokenRepository).deleteByUserId(1L);

        ArgumentCaptor<EmailVerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        EmailVerificationTokenEntity saved = tokenCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getToken()).hasSize(64); // 32 random bytes, hex-encoded
        assertThat(saved.getExpiresAt()).isAfter(LocalDateTime.now().plusHours(23));

        ArgumentCaptor<VerificationEmailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(VerificationEmailRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        VerificationEmailRequestedEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(1L);
        assertThat(event.email()).isEqualTo("user@example.com");
        assertThat(event.verificationLink()).contains(saved.getToken());
    }

    @Test
    void sendVerificationEmail_existingTokenWithinCooldown_throwsTooManyRequests() {
        EmailVerificationTokenEntity existing = new EmailVerificationTokenEntity();
        existing.setUser(user);
        existing.setToken("old-token");
        existing.setExpiresAt(LocalDateTime.now().plusHours(24));
        ReflectionTestUtils.setField(existing, "createdAt", LocalDateTime.now().minusSeconds(10)); // 10s ago, cooldown is 60s

        given(tokenRepository.findByUserId(1L)).willReturn(Optional.of(existing));

        assertThatThrownBy(() -> emailVerificationService.sendVerificationEmail(user))
                .isInstanceOf(GlobalExceptionHandler.TooManyRequestsException.class);

        verify(tokenRepository, never()).deleteByUserId(anyLong());
        verify(tokenRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void sendVerificationEmail_existingTokenPastCooldown_proceedsNormally() {
        EmailVerificationTokenEntity existing = new EmailVerificationTokenEntity();
        existing.setUser(user);
        existing.setToken("old-token");
        existing.setExpiresAt(LocalDateTime.now().plusHours(24));
        ReflectionTestUtils.setField(existing, "createdAt", LocalDateTime.now().minusSeconds(90)); // past 60s cooldown

        given(tokenRepository.findByUserId(1L)).willReturn(Optional.of(existing));

        emailVerificationService.sendVerificationEmail(user);

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(EmailVerificationTokenEntity.class));
        verify(eventPublisher).publishEvent(any(VerificationEmailRequestedEvent.class));
    }

    @Test
    void verifyToken_valid_marksUserVerifiedAndDeletesToken() {
        EmailVerificationTokenEntity tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken("good-token");
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(1));

        given(tokenRepository.findByToken("good-token")).willReturn(Optional.of(tokenEntity));

        emailVerificationService.verifyToken("good-token");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).delete(tokenEntity);
    }

    @Test
    void verifyToken_invalidToken_throwsInvalidTokenException() {
        given(tokenRepository.findByToken("bad-token")).willReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.verifyToken("bad-token"))
                .isInstanceOf(GlobalExceptionHandler.InvalidTokenException.class);

        verifyNoInteractions(userRepository);
    }

    @Test
    void verifyToken_expiredToken_deletesTokenAndThrows() {
        EmailVerificationTokenEntity expired = new EmailVerificationTokenEntity();
        expired.setUser(user);
        expired.setToken("expired-token");
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        given(tokenRepository.findByToken("expired-token")).willReturn(Optional.of(expired));

        assertThatThrownBy(() -> emailVerificationService.verifyToken("expired-token"))
                .isInstanceOf(GlobalExceptionHandler.InvalidTokenException.class);

        verify(tokenRepository).delete(expired);
        verifyNoInteractions(userRepository);
        assertThat(user.isEmailVerified()).isFalse();
    }

    @Test
    void sendDeletionWarningEmail_noExistingToken_generatesNewTokenAndPublishesEvent() {
        given(tokenRepository.findByUserId(1L)).willReturn(Optional.empty());

        emailVerificationService.sendDeletionWarningEmail(user);

        verify(tokenRepository).deleteByUserId(1L);

        ArgumentCaptor<EmailVerificationTokenEntity> tokenCaptor = ArgumentCaptor.forClass(EmailVerificationTokenEntity.class);
        verify(tokenRepository).save(tokenCaptor.capture());
        EmailVerificationTokenEntity saved = tokenCaptor.getValue();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getToken()).hasSize(64);

        ArgumentCaptor<DeletionWarningEmailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(DeletionWarningEmailRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        DeletionWarningEmailRequestedEvent event = eventCaptor.getValue();
        assertThat(event.userId()).isEqualTo(1L);
        assertThat(event.email()).isEqualTo("user@example.com");
        assertThat(event.verificationLink()).contains(saved.getToken());
    }

    @Test
    void sendDeletionWarningEmail_expiredExistingToken_generatesNewTokenAndPublishesEvent() {
        EmailVerificationTokenEntity expired = new EmailVerificationTokenEntity();
        expired.setUser(user);
        expired.setToken("expired-token");
        expired.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        given(tokenRepository.findByUserId(1L)).willReturn(Optional.of(expired));

        emailVerificationService.sendDeletionWarningEmail(user);

        verify(tokenRepository).deleteByUserId(1L);
        verify(tokenRepository).save(any(EmailVerificationTokenEntity.class));
        verify(eventPublisher).publishEvent(any(DeletionWarningEmailRequestedEvent.class));
    }

    @Test
    void sendDeletionWarningEmail_validExistingToken_reusesTokenWithoutRegenerating() {
        EmailVerificationTokenEntity existing = new EmailVerificationTokenEntity();
        existing.setUser(user);
        existing.setToken("still-valid-token");
        existing.setExpiresAt(LocalDateTime.now().plusHours(12));

        given(tokenRepository.findByUserId(1L)).willReturn(Optional.of(existing));

        emailVerificationService.sendDeletionWarningEmail(user);

        verify(tokenRepository, never()).deleteByUserId(anyLong());
        verify(tokenRepository, never()).save(any());

        ArgumentCaptor<DeletionWarningEmailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(DeletionWarningEmailRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().verificationLink()).contains("still-valid-token");
    }

    @Test
    void sendDeletionWarningEmail_doesNotEnforceResendCooldown() {
        // Unlike sendVerificationEmail, this is a system-triggered job, not a user resend click,
        // and must not be blocked by the 60s cooldown.
        EmailVerificationTokenEntity existing = new EmailVerificationTokenEntity();
        existing.setUser(user);
        existing.setToken("just-created-token");
        existing.setExpiresAt(LocalDateTime.now().plusHours(23));
        ReflectionTestUtils.setField(existing, "createdAt", LocalDateTime.now().minusSeconds(5)); // well within cooldown

        given(tokenRepository.findByUserId(1L)).willReturn(Optional.of(existing));

        assertThatCode(() -> emailVerificationService.sendDeletionWarningEmail(user))
                .doesNotThrowAnyException();

        verify(eventPublisher).publishEvent(any(DeletionWarningEmailRequestedEvent.class));
    }
}
