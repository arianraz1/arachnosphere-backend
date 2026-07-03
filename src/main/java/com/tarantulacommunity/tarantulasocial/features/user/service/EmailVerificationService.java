package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.EmailVerificationTokenEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.event.VerificationEmailRequestedEvent;
import com.tarantulacommunity.tarantulasocial.features.user.repository.EmailVerificationTokenRepository;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.base-url}")
    private String baseUrl;

    private static final int TOKEN_EXPIRY_HOURS = 24;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    @Transactional
    public void sendVerificationEmail(UserEntity user) {
        tokenRepository.findByUserId(user.getId()).ifPresent(existing -> {
            LocalDateTime cooldownExpiresAt = existing.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS);
            if (cooldownExpiresAt.isAfter(LocalDateTime.now())) {
                throw new GlobalExceptionHandler.TooManyRequestsException(
                        "Please wait before requesting another verification email");
            }
        });

        tokenRepository.deleteByUserId(user.getId());

        String token = generateToken();

        EmailVerificationTokenEntity tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setUser(user);
        tokenEntity.setToken(token);
        tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
        tokenRepository.save(tokenEntity);

        String verificationLink = baseUrl + "/api/auth/verify-email?token=" + token;

        eventPublisher.publishEvent(new VerificationEmailRequestedEvent(
                user.getId(), user.getEmail(), user.getUsername(), verificationLink));
    }

    @Transactional
    public void verifyToken(String token) {
        EmailVerificationTokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new GlobalExceptionHandler.InvalidTokenException("Invalid or expired verification token"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            throw new GlobalExceptionHandler.InvalidTokenException("Verification token has expired");
        }

        UserEntity user = tokenEntity.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(tokenEntity);
        log.info("Email verified for user: {}", user.getEmail());
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }
}