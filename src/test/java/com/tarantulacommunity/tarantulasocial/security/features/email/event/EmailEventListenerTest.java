package com.tarantulacommunity.tarantulasocial.security.features.email.event;

import com.tarantulacommunity.tarantulasocial.security.features.email.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailEventListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailEventListener listener;

    @Test
    void onVerificationEmailRequested_callsEmailServiceWithCorrectArgs() {
        VerificationEmailRequestedEvent event = new VerificationEmailRequestedEvent(
                1L, "user@example.com", "user", "http://localhost:8080/api/auth/verify-email?token=abc");

        listener.onVerificationEmailRequested(event);

        verify(emailService).sendVerificationEmail(
                "user@example.com", "user", "http://localhost:8080/api/auth/verify-email?token=abc");
    }

    @Test
    void onVerificationEmailRequested_swallowsEmailServiceException() {
        VerificationEmailRequestedEvent event = new VerificationEmailRequestedEvent(
                1L, "user@example.com", "user", "http://localhost:8080/verify?token=abc");
        willThrow(new RuntimeException("SendGrid down"))
                .given(emailService).sendVerificationEmail(anyString(), anyString(), anyString());

        assertThatCode(() -> listener.onVerificationEmailRequested(event))
                .doesNotThrowAnyException();
    }

    @Test
    void onDeletionWarningEmailRequested_callsEmailServiceWithCorrectArgs() {
        DeletionWarningEmailRequestedEvent event = new DeletionWarningEmailRequestedEvent(
                1L, "user@example.com", "user", "http://localhost:8080/api/auth/verify-email?token=xyz");

        listener.onDeletionWarningEmailRequested(event);

        verify(emailService).sendDeletionWarningEmail(
                "user@example.com", "user", "http://localhost:8080/api/auth/verify-email?token=xyz");
    }

    @Test
    void onDeletionWarningEmailRequested_swallowsEmailServiceException() {
        DeletionWarningEmailRequestedEvent event = new DeletionWarningEmailRequestedEvent(
                1L, "user@example.com", "user", "http://localhost:8080/verify?token=xyz");
        willThrow(new RuntimeException("SendGrid down"))
                .given(emailService).sendDeletionWarningEmail(anyString(), anyString(), anyString());

        assertThatCode(() -> listener.onDeletionWarningEmailRequested(event))
                .doesNotThrowAnyException();
    }
}