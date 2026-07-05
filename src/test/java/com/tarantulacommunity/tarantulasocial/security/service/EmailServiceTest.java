package com.tarantulacommunity.tarantulasocial.security.service;

import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private SendGrid sendGrid;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(
                "test-api-key",
                "noreply@arachnosphere.com",
                "Arachnosphere"
        );

        ReflectionTestUtils.setField(emailService, "sendGrid", sendGrid);
    }

    @Test
    void sendVerificationEmail_shouldCallSendGridApi() throws Exception {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        Response response = new Response();
        response.setStatusCode(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        emailService.sendVerificationEmail(toEmail, username, verificationLink);

        verify(sendGrid).api(any(Request.class));
    }

    @Test
    void sendVerificationEmail_shouldBuildCorrectEmailContent() throws Exception {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        Response response = new Response();
        response.setStatusCode(202);
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        ArgumentCaptor<Request> requestCaptor = ArgumentCaptor.forClass(Request.class);

        emailService.sendVerificationEmail(toEmail, username, verificationLink);

        verify(sendGrid).api(requestCaptor.capture());

        Request capturedRequest = requestCaptor.getValue();
        assertThat(capturedRequest.getMethod()).isEqualTo(com.sendgrid.Method.POST);
        assertThat(capturedRequest.getEndpoint()).isEqualTo("mail/send");

        String body = capturedRequest.getBody();
        assertThat(body).contains(username);
        assertThat(body).contains(verificationLink);
        assertThat(body).contains("Verify your Arachnosphere account");
    }

    @Test
    void sendVerificationEmail_shouldThrowException_whenSendGridReturnsError() throws Exception {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        Response response = new Response();
        response.setStatusCode(400);
        response.setBody("Invalid API key");
        when(sendGrid.api(any(Request.class))).thenReturn(response);

        assertThatThrownBy(() ->
                emailService.sendVerificationEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
    }

    @Test
    void sendVerificationEmail_shouldThrowException_whenIOExceptionOccurs() throws Exception {
        String toEmail = "test@example.com";
        String username = "testuser";
        String verificationLink = "https://arachnosphere.com/verify?token=abc123";

        when(sendGrid.api(any(Request.class))).thenThrow(new IOException("Network error"));

        assertThatThrownBy(() ->
                emailService.sendVerificationEmail(toEmail, username, verificationLink)
        ).isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to send verification email");
    }
}
