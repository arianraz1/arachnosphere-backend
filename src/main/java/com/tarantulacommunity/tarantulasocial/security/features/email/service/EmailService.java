package com.tarantulacommunity.tarantulasocial.security.features.email.service;

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
        String subject = "Verify your Arachnosphere account 🕸️";

        String htmlContent = """
        <!DOCTYPE html>
               <html lang="en">
               <head>
                   <meta charset="UTF-8" />
                   <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                   <title>Verify your Arachnosphere account 🕸️</title>
               </head>
               <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #0a0a0a;">
                   <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0; background-color: #0a0a0a;">
                       <tr>
                           <td align="center">
                               <table width="600" cellpadding="0" cellspacing="0" style="background-color: #171412; border: 1px solid #292524; border-radius: 12px; overflow: hidden; max-width: 600px; width: 100%%;">

                                   <!-- Header -->
                                   <tr>
                                       <td style="background-color: #0a0a0a; padding: 40px 40px 24px 40px; text-align: center; border-bottom: 1px solid #292524;">
                                           <h1 style="margin: 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;">
                                               🕷️ Arachnosphere
                                           </h1>
                                       </td>
                                   </tr>

                                   <!-- Body -->
                                   <tr>
                                       <td style="padding: 24px 40px 40px 40px;">
                                           <h2 style="margin: 0 0 16px 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 20px; font-weight: bold;">
                                               Welcome, %s!
                                           </h2>
                                           <p style="margin: 0 0 12px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                               Glad you're here! This web was woven straight from some spinnerets to connect keepers worldwide.
                                           </p>
                                           <p style="margin: 0 0 24px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                               Tap below to verify your email and join the community.
                                           </p>

                                           <!-- CTA Button -->
                                           <table cellpadding="0" cellspacing="0" style="margin-bottom: 32px;">
                                               <tr>
                                                   <td style="border-radius: 8px; background-color: #f97316;">
                                                       <a href="%s" style="display: inline-block; padding: 14px 32px; color: #1c1917; font-size: 15px; font-weight: bold; text-decoration: none; border-radius: 8px;">
                                                           Verify My Email
                                                       </a>
                                                   </td>
                                               </tr>
                                           </table>

                                           <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px;">
                                               Or copy and paste this link into your browser:
                                           </p>
                                           <p style="margin: 0 0 32px 0;">
                                               <a href="%s" style="color: #f97316; font-size: 13px; word-break: break-all;">
                                                   %s
                                               </a>
                                           </p>

                                           <!-- Divider -->
                                           <hr style="border: none; border-top: 1px solid #292524; margin: 0 0 24px 0;" />

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
                                       <td style="background-color: #0a0a0a; padding: 24px 40px; text-align: center; border-top: 1px solid #292524;">
                                           <p style="margin: 0; color: #78716c; font-size: 12px;">
                                               © 2026 Arachnosphere. All rights reserved.
                                           </p>
                                           <p style="margin: 8px 0 0 0; color: #78716c; font-size: 12px;">
                                               You're receiving this email because you signed up at\s
                                               <a href="%s" style="color: #a8a29e; text-decoration: none;">arachnosphere.com</a>
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

    public void sendDeletionWarningEmail(String toEmail, String username, String verificationLink) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Your Arachnosphere account will be deleted tomorrow 🕸️";

        String htmlContent = """
    <!DOCTYPE html>
           <html lang="en">
           <head>
               <meta charset="UTF-8" />
               <meta name="viewport" content="width=device-width, initial-scale=1.0" />
               <title>Your Arachnosphere account will be deleted tomorrow 🕸️</title>
           </head>
           <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #0a0a0a;">
               <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0; background-color: #0a0a0a;">
                   <tr>
                       <td align="center">
                           <table width="600" cellpadding="0" cellspacing="0" style="background-color: #171412; border: 1px solid #292524; border-radius: 12px; overflow: hidden; max-width: 600px; width: 100%%;">

                               <!-- Header -->
                               <tr>
                                   <td style="background-color: #0a0a0a; padding: 40px 40px 24px 40px; text-align: center; border-bottom: 1px solid #292524;">
                                       <h1 style="margin: 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;">
                                           🕷️ Arachnosphere
                                       </h1>
                                   </td>
                               </tr>

                               <!-- Body -->
                               <tr>
                                   <td style="padding: 24px 40px 40px 40px;">
                                       <h2 style="margin: 0 0 16px 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 20px; font-weight: bold;">
                                           Hi %s, your account is about to be removed
                                       </h2>
                                       <p style="margin: 0 0 12px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                           You registered an Arachnosphere account but never verified your email. Unverified accounts are automatically removed after 7 days.
                                       </p>
                                       <p style="margin: 0 0 24px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                           Your account will be deleted <strong style="color: #fafaf9;">tomorrow</strong> unless you verify your email now.
                                       </p>

                                       <!-- CTA Button -->
                                       <table cellpadding="0" cellspacing="0" style="margin-bottom: 32px;">
                                           <tr>
                                               <td style="border-radius: 8px; background-color: #f97316;">
                                                   <a href="%s" style="display: inline-block; padding: 14px 32px; color: #1c1917; font-size: 15px; font-weight: bold; text-decoration: none; border-radius: 8px;">
                                                       Verify My Email
                                                   </a>
                                               </td>
                                           </tr>
                                       </table>

                                       <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px;">
                                           Or copy and paste this link into your browser:
                                       </p>
                                       <p style="margin: 0 0 32px 0;">
                                           <a href="%s" style="color: #f97316; font-size: 13px; word-break: break-all;">
                                               %s
                                           </a>
                                       </p>

                                       <!-- Divider -->
                                       <hr style="border: none; border-top: 1px solid #292524; margin: 0 0 24px 0;" />

                                       <p style="margin: 0; color: #78716c; font-size: 13px; line-height: 1.5;">
                                           If you didn't create this account, no action is needed — it will be removed automatically.
                                       </p>
                                   </td>
                               </tr>

                               <!-- Footer -->
                               <tr>
                                   <td style="background-color: #0a0a0a; padding: 24px 40px; text-align: center; border-top: 1px solid #292524;">
                                       <p style="margin: 0; color: #78716c; font-size: 12px;">
                                           © 2026 Arachnosphere. All rights reserved.
                                       </p>
                                       <p style="margin: 8px 0 0 0; color: #78716c; font-size: 12px;">
                                           You're receiving this email because you signed up at\s
                                           <a href="%s" style="color: #a8a29e; text-decoration: none;">arachnosphere.com</a>
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
                throw new RuntimeException("Failed to send deletion warning email");
            }

            log.info("Deletion warning email sent to {}", toEmail);
        } catch (IOException e) {
            log.error("Failed to send deletion warning email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send deletion warning email", e);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String username, String resetLink) {
        Email from = new Email(fromEmail, fromName);
        Email to = new Email(toEmail);
        String subject = "Reset your Arachnosphere password 🕸️️";

        String htmlContent = """
    <!DOCTYPE html>
           <html lang="en">
           <head>
               <meta charset="UTF-8" />
               <meta name="viewport" content="width=device-width, initial-scale=1.0" />
               <title>Reset your Arachnosphere password 🕸️</title>
           </head>
           <body style="margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #0a0a0a;">
               <table width="100%%" cellpadding="0" cellspacing="0" style="padding: 40px 0; background-color: #0a0a0a;">
                   <tr>
                       <td align="center">
                           <table width="600" cellpadding="0" cellspacing="0" style="background-color: #171412; border: 1px solid #292524; border-radius: 12px; overflow: hidden; max-width: 600px; width: 100%%;">

                               <!-- Header -->
                               <tr>
                                   <td style="background-color: #0a0a0a; padding: 40px 40px 24px 40px; text-align: center; border-bottom: 1px solid #292524;">
                                       <h1 style="margin: 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 28px; font-weight: bold; letter-spacing: -0.5px;">
                                           🕷️ Arachnosphere
                                       </h1>
                                   </td>
                               </tr>

                               <!-- Body -->
                               <tr>
                                   <td style="padding: 24px 40px 40px 40px;">
                                       <h2 style="margin: 0 0 16px 0; color: #fafaf9; font-family: Georgia, 'Times New Roman', serif; font-size: 20px; font-weight: bold;">
                                           Password Reset Request
                                       </h2>
                                       <p style="margin: 0 0 12px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                           Hi %s, we received a request to reset your Arachnosphere password.
                                       </p>
                                       <p style="margin: 0 0 24px 0; color: #a8a29e; font-size: 15px; line-height: 1.6;">
                                           Tap below to choose a new password. If you didn't request this, you can safely ignore this email.
                                       </p>

                                       <!-- CTA Button -->
                                       <table cellpadding="0" cellspacing="0" style="margin-bottom: 32px;">
                                           <tr>
                                               <td style="border-radius: 8px; background-color: #f97316;">
                                                   <a href="%s" style="display: inline-block; padding: 14px 32px; color: #1c1917; font-size: 15px; font-weight: bold; text-decoration: none; border-radius: 8px;">
                                                       Reset Password
                                                   </a>
                                               </td>
                                           </tr>
                                       </table>

                                       <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px;">
                                           Or copy and paste this link into your browser:
                                       </p>
                                       <p style="margin: 0 0 32px 0;">
                                           <a href="%s" style="color: #f97316; font-size: 13px; word-break: break-all;">
                                               %s
                                           </a>
                                       </p>

                                       <!-- Divider -->
                                       <hr style="border: none; border-top: 1px solid #292524; margin: 0 0 24px 0;" />

                                       <p style="margin: 0 0 8px 0; color: #78716c; font-size: 13px; line-height: 1.5;">
                                           ⏱ This link expires in <strong style="color: #a8a29e;">1 hour</strong>.
                                       </p>
                                       <p style="margin: 0; color: #78716c; font-size: 13px; line-height: 1.5;">
                                           If you didn't request a password reset, you can safely ignore this email. Your password will not be changed.
                                       </p>
                                   </td>
                               </tr>

                               <!-- Footer -->
                               <tr>
                                   <td style="background-color: #0a0a0a; padding: 24px 40px; text-align: center; border-top: 1px solid #292524;">
                                       <p style="margin: 0; color: #78716c; font-size: 12px;">
                                           © 2026 Arachnosphere. All rights reserved.
                                       </p>
                                       <p style="margin: 8px 0 0 0; color: #78716c; font-size: 12px;">
                                           You're receiving this email because you requested a password reset at\s
                                           <a href="%s" style="color: #a8a29e; text-decoration: none;">arachnosphere.com</a>
                                       </p>
                                   </td>
                               </tr>

                           </table>
                       </td>
                   </tr>
               </table>
           </body>
           </html>
    """.formatted(username, resetLink, resetLink, resetLink, "https://arachnosphere.com");

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
                throw new RuntimeException("Failed to send password reset email");
            }

            log.info("Password reset email sent to {}", toEmail);
        } catch (IOException e) {
            log.error("Failed to send password reset email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}