package com.tarantulacommunity.tarantulasocial.security.features.email.cleanup;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.features.email.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UnverifiedAccountCleanupService {

    private static final int UNVERIFIED_ACCOUNT_EXPIRY_DAYS = 7;
    private static final int REMINDER_DAY = 6;

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    @Scheduled(cron = "0 0 3 * * *") // daily at 3am server time
    @Transactional
    public void deleteExpiredUnverifiedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(UNVERIFIED_ACCOUNT_EXPIRY_DAYS);

        List<UserEntity> expired = userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(cutoff);

        if (expired.isEmpty()) {
            log.info("Unverified account cleanup: no expired accounts found");
            return;
        }

        log.info("Unverified account cleanup: deleting {} expired unverified accounts", expired.size());
        userRepository.deleteAll(expired);
    }

    @Scheduled(cron = "0 0 9 * * *") // daily at 9am server time, separate from deletion run
    @Transactional
    public void remindUnverifiedAccounts() {
        LocalDateTime windowStart = LocalDateTime.now().minusDays(REMINDER_DAY + 1);
        LocalDateTime windowEnd = LocalDateTime.now().minusDays(REMINDER_DAY);

        List<UserEntity> toRemind = userRepository.findByEmailVerifiedFalseAndCreatedAtBetween(windowStart, windowEnd);

        if (toRemind.isEmpty()) {
            log.info("Unverified account reminder: no accounts in reminder window");
            return;
        }

        log.info("Unverified account reminder: sending {} deletion-warning emails", toRemind.size());
        toRemind.forEach(emailVerificationService::sendDeletionWarningEmail);
    }
}