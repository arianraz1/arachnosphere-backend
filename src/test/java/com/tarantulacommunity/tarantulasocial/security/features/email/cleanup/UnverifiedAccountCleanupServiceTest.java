package com.tarantulacommunity.tarantulasocial.security.features.email.cleanup;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.features.email.service.EmailVerificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnverifiedAccountCleanupServiceTest {

    @Mock private UserRepository userRepository;
    @Mock
    private EmailVerificationService emailVerificationService;

    @InjectMocks
    private UnverifiedAccountCleanupService cleanupService;

    @Test
    void deleteExpiredUnverifiedAccounts_noExpiredAccounts_doesNothing() {
        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(List.of());

        cleanupService.deleteExpiredUnverifiedAccounts();

        verify(userRepository, never()).deleteAll(anyList());
    }

    @Test
    void deleteExpiredUnverifiedAccounts_hasExpiredAccounts_deletesThem() {
        UserEntity expired1 = new UserEntity();
        expired1.setId(1L);
        UserEntity expired2 = new UserEntity();
        expired2.setId(2L);
        List<UserEntity> expired = List.of(expired1, expired2);

        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(any(LocalDateTime.class)))
                .willReturn(expired);

        cleanupService.deleteExpiredUnverifiedAccounts();

        verify(userRepository).deleteAll(expired);
    }

    @Test
    void deleteExpiredUnverifiedAccounts_usesSevenDayCutoff() {
        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBefore(cutoffCaptor.capture()))
                .willReturn(List.of());

        LocalDateTime before = LocalDateTime.now().minusDays(7);
        cleanupService.deleteExpiredUnverifiedAccounts();
        LocalDateTime after = LocalDateTime.now().minusDays(7);

        LocalDateTime capturedCutoff = cutoffCaptor.getValue();
        assertThat(capturedCutoff).isBetween(before.minusSeconds(2), after.plusSeconds(2));
    }

    @Test
    void remindUnverifiedAccounts_noAccountsInWindow_doesNothing() {
        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(List.of());

        cleanupService.remindUnverifiedAccounts();

        verifyNoInteractions(emailVerificationService);
    }

    @Test
    void remindUnverifiedAccounts_accountsInWindow_sendsWarningToEach() {
        UserEntity toRemind1 = new UserEntity();
        toRemind1.setId(1L);
        UserEntity toRemind2 = new UserEntity();
        toRemind2.setId(2L);
        List<UserEntity> toRemind = List.of(toRemind1, toRemind2);

        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBetween(
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .willReturn(toRemind);

        cleanupService.remindUnverifiedAccounts();

        verify(emailVerificationService).sendDeletionWarningEmail(toRemind1);
        verify(emailVerificationService).sendDeletionWarningEmail(toRemind2);
    }

    @Test
    void remindUnverifiedAccounts_usesDay6Window() {
        ArgumentCaptor<LocalDateTime> startCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        given(userRepository.findByEmailVerifiedFalseAndCreatedAtBetween(
                startCaptor.capture(), endCaptor.capture()))
                .willReturn(List.of());

        cleanupService.remindUnverifiedAccounts();

        LocalDateTime expectedStart = LocalDateTime.now().minusDays(7);
        LocalDateTime expectedEnd = LocalDateTime.now().minusDays(6);

        assertThat(startCaptor.getValue()).isBetween(expectedStart.minusSeconds(2), expectedStart.plusSeconds(2));
        assertThat(endCaptor.getValue()).isBetween(expectedEnd.minusSeconds(2), expectedEnd.plusSeconds(2));
    }
}