package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;
    private final String fromEmail;
    private final String fromName;

    public EmailService(
            @Value("${sendgrid.api.key}") String apiKey,
            @Value("${sendgrid.from.email}") String fromEmail,
            @Value("${sendgrid.from.name}") String fromName) {
        this.sendGrid = new SendGrid(apiKey);
        this.fromEmail = fromEmail;
        this.fromName = fromName;
    }

    public void sendVerificationEmail(String toEmail, String username, String verificationLink) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Verify your Arachnosphere account";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                    <h2>Welcome to Arachnosphere, %s!</h2>
                    <p>Thanks for registering. Please verify your email address by clicking the button below.</p>
                    <p>
                        <a href="%s"
                           style="background-color: #f59e0b; color: #1c1917; padding: 12px 24px;
                                  text-decoration: none; border-radius: 6px; font-weight: bold;">
                            Verify Email
                        </a>
                    </p>
                    <p>Or copy and paste this link into your browser:</p>
                    <p><a href="%s">%s</a></p>
                    <p>This link expires in 24 hours.</p>
                    <p>If you did not create an account, you can ignore this email.</p>
                </div>
                """.formatted(username, verificationLink, verificationLink, verificationLink);

        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        try {
            Request sendGridRequest = new Request();
            sendGridRequest.setMethod(Method.POST);
            sendGridRequest.setEndpoint("mail/send");
            sendGridRequest.setBody(mail.build());
            Response sendGridResponse = sendGrid.api(sendGridRequest);

            if (sendGridResponse.getStatusCode() >= 400) {
                log.error("SendGrid error: {} - {}", sendGridResponse.getStatusCode(), sendGridResponse.getBody());
                throw new RuntimeException("Failed to send verification email");
            }

            log.info("Verification email sent to {}", toEmail);
        } catch (IOException e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}