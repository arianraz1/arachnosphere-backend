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
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8" />
            <meta name="viewport" content="width=device-width, initial-scale=1.0" />
            <title>Verify your Arachnosphere account</title>
        </head>
        <body style="margin: 0; padding: 0; font-family: Arial, sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0" style="background-color: #292524; border-radius: 12px; overflow: hidden; max-width: 600px; width: 100%%;">

                            <!-- Header -->
                            <tr>
                                <td style="background-color: #f59e0b; padding: 32px 40px; text-align: center;">
                                    <h1 style="margin: 0; color: #1c1917; font-size: 24px; font-weight: bold; letter-spacing: -0.5px;">
                                        🕷 Arachnosphere
                                    </h1>
                                </td>
                            </tr>

                            <!-- Body -->
                            <tr>
                                <td style="padding: 40px;">
                                    <h2 style="margin: 0 0 16px 0; color: #fafaf9; font-size: 20px; font-weight: bold;">
                                        Welcome, %s!
                                    </h2>
                                    <p style="margin: 0 0 24px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                        Thanks for joining Arachnosphere — the community for tarantula enthusiasts.
                                        Please verify your email address to activate your account and get full access.
                                    </p>

                                    <!-- CTA Button -->
                                    <table cellpadding="0" cellspacing="0" style="margin-bottom: 32px;">
                                        <tr>
                                            <td style="border-radius: 8px; background-color: #f59e0b;">
                                                <a href="%s"
                                                   style="display: inline-block; padding: 14px 32px; color: #1c1917;
                                                          font-size: 15px; font-weight: bold; text-decoration: none;
                                                          border-radius: 8px;">
                                                    Verify My Email
                                                </a>
                                            </td>
                                        </tr>
                                    </table>

                                    <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px;">
                                        Or copy and paste this link into your browser:
                                    </p>
                                    <p style="margin: 0 0 32px 0;">
                                        <a href="%s"
                                           style="color: #f59e0b; font-size: 13px; word-break: break-all;">
                                            %s
                                        </a>
                                    </p>

                                    <!-- Divider -->
                                    <hr style="border: none; border-top: 1px solid #44403c; margin: 0 0 24px 0;" />

                                    <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px; line-height: 1.5;">
                                        ⏱ This link expires in <strong style="color: #a8a29e;">24 hours</strong>.
                                    </p>
                                    <p style="margin: 0; color: #78716c; font-size: 13px; line-height: 1.5;">
                                        If you didn't create an account with Arachnosphere, you can safely ignore this email.
                                    </p>
                                </td>
                            </tr>

                            <!-- Footer -->
                            <tr>
                                <td style="background-color: #1c1917; padding: 24px 40px; text-align: center;">
                                    <p style="margin: 0; color: #57534e; font-size: 12px;">
                                        © 2026 Arachnosphere. All rights reserved.
                                    </p>
                                    <p style="margin: 8px 0 0 0; color: #57534e; font-size: 12px;">
                                        You're receiving this email because you signed up at
                                        <a href="%s" style="color: #78716c; text-decoration: none;">arachnosphere.com</a>
                                    </p>
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """.formatted(username, verificationLink, verificationLink, verificationLink, "https://arachnosphere.com");

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