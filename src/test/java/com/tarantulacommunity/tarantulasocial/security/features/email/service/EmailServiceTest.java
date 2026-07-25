package com.tarantulacommunity.tarantulasocial.security.features.email.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(
                "test-api-key",
                "noreply@arachnosphere.com",
                "Arachnosphere"
        );

        // EmailService builds its own RestClient internally in the constructor,
        // so we inject our mock in place of the real one.
        ReflectionTestUtils.setField(emailService, "restClient", restClient);
    }

    @SuppressWarnings("unchecked")
    private void mockSuccessChain() {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }

    @SuppressWarnings("unchecked")
    private void mockErrorChain(RuntimeException toThrow) {
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(Map.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toBodilessEntity()).thenThrow(toThrow);
    }

    @Test
    void sendVerificationEmail_shouldCallResendApi() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockSuccessChain();

        emailService.sendVerificationEmail(toEmail, username, verificationLink);

        verify(restClient).post();
        verify(requestBodySpec).retrieve();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendVerificationEmail_shouldBuildCorrectEmailContent() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockSuccessChain();

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);

        emailService.sendVerificationEmail(toEmail, username, verificationLink);

        verify(requestBodyUriSpec).body(bodyCaptor.capture());

        Map<String, Object> body = bodyCaptor.getValue();

        assertThat(body).isNotNull();
        assertThat(body.get("to")).isEqualTo(List.of(toEmail));
        assertThat(body.get("from")).isEqualTo("Arachnosphere <noreply@arachnosphere.com>");
        assertThat((String) body.get("subject")).contains("Verify your Arachnosphere account");
        assertThat((String) body.get("html")).contains(username);
        assertThat((String) body.get("html")).contains(verificationLink);
    }

    @Test
    void sendVerificationEmail_shouldThrowException_whenResendReturnsError() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockErrorChain(new RestClientResponseException(
                "400 Bad Request", 400, "Bad Request", HttpHeaders.EMPTY, "Invalid API key".getBytes(), null));

        assertThatThrownBy(() ->
                emailService.sendVerificationEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
    }
    @Test
    void sendVerificationEmail_shouldThrowException_whenRestClientExceptionOccurs() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockErrorChain(new ResourceAccessException("Network error"));

        assertThatThrownBy(() ->
                emailService.sendVerificationEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
    }

    @Test
    void sendDeletionWarningEmail_shouldCallResendApi() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockSuccessChain();

        emailService.sendDeletionWarningEmail(toEmail, username, verificationLink);

        verify(restClient).post();
        verify(requestBodySpec).retrieve();
    }

    @Test
    @SuppressWarnings("unchecked")
    void sendDeletionWarningEmail_shouldBuildCorrectEmailContent() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockSuccessChain();

        ArgumentCaptor<Map<String, Object>> bodyCaptor = ArgumentCaptor.forClass(Map.class);

        emailService.sendDeletionWarningEmail(toEmail, username, verificationLink);

        verify(requestBodyUriSpec).body(bodyCaptor.capture());

        Map<String, Object> body = bodyCaptor.getValue();

        assertThat(body).isNotNull();
        assertThat((String) body.get("html")).contains(username);
        assertThat((String) body.get("html")).contains(verificationLink);
        assertThat((String) body.get("html")).contains("will be deleted");
    }

    @Test
    void sendDeletionWarningEmail_shouldThrowException_whenResendReturnsError() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockErrorChain(new RestClientResponseException(
                "400 Bad Request", 400, "Bad Request", HttpHeaders.EMPTY, "Invalid API key".getBytes(), null));

        assertThatThrownBy(() ->
                emailService.sendDeletionWarningEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send deletion warning email");
    }

    @Test
    void sendDeletionWarningEmail_shouldThrowException_whenRestClientExceptionOccurs() {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        mockErrorChain(new ResourceAccessException("Network error"));

        assertThatThrownBy(() ->
                emailService.sendDeletionWarningEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send deletion warning email");
    }
}