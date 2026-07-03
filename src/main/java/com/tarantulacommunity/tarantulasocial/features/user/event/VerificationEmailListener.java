package com.tarantulacommunity.tarantulasocial.features.user.event;

import com.tarantulacommunity.tarantulasocial.features.user.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerificationEmailListener {

    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onVerificationEmailRequested(VerificationEmailRequestedEvent event) {
        try {
            emailService.sendVerificationEmail(event.email(), event.username(), event.verificationLink());
        } catch (RuntimeException ex) {
            // Token is already committed to the DB at this point — the user can still verify
            // manually if they have the link, or hit /resend-verification. Don't let a
            // SendGrid failure surface as a 500 on register/resend, since the DB write already succeeded.
            log.error("Failed to send verification email to user id {}: {}", event.userId(), ex.getMessage());
        }
    }
}