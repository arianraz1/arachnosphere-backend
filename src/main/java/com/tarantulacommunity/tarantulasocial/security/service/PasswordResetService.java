package com.tarantulacommunity.tarantulasocial.security.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.entity.PasswordResetTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.cors.allowed-origin}")
    private String frontendUrl;

    private static final int TOKEN_EXPIRY_HOURS = 1;

    @Transactional
    public void sendResetEmail(String email) {
        userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
            tokenRepository.deleteByUserId(user.getId());

            String token = generateToken();

            PasswordResetTokenEntity tokenEntity = new PasswordResetTokenEntity();
            tokenEntity.setUser(user);
            tokenEntity.setToken(token);
            tokenEntity.setExpiresAt(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS));
            tokenRepository.save(tokenEntity);

            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), user.getUsername(), resetLink);
            log.info("Password reset email sent to {}", email);
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity tokenEntity = tokenRepository.findByToken(token)
                .orElseThrow(() -> new GlobalExceptionHandler.InvalidTokenException("Invalid or expired reset token"));

        if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            tokenRepository.delete(tokenEntity);
            throw new GlobalExceptionHandler.InvalidTokenException("Reset token has expired");
        }

        UserEntity user = tokenEntity.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(tokenEntity);
        log.info("Password reset successfully for user: {}", user.getEmail());
    }

    private String generateToken() {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        return HexFormat.of().formatHex(randomBytes);
    }
}
