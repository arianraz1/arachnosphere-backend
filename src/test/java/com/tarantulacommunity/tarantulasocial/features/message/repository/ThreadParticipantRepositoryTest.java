package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadParticipantEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ThreadParticipantRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ThreadParticipantRepository threadParticipantRepository;

    private UserEntity userA;
    private UserEntity userB;
    private ThreadEntity thread;

    @BeforeEach
    void setUp() {
        userA = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        userB = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        thread = em.persistFlushFind(TestUtilities.validThread(userA));
    }

    // --- findByThread_IdAndUser_Id ---

    @Test
    void findByThreadIdAndUserId_returnsParticipant_whenExists() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userA, LocalDateTime.now()));

        Optional<ThreadParticipantEntity> result =
                threadParticipantRepository.findByThread_IdAndUser_Id(thread.getId(), userA.getId());

        assertTrue(result.isPresent());
    }

    @Test
    void findByThreadIdAndUserId_returnsEmpty_whenNotExists() {
        Optional<ThreadParticipantEntity> result =
                threadParticipantRepository.findByThread_IdAndUser_Id(thread.getId(), userA.getId());

        assertTrue(result.isEmpty());
    }

    // --- existsByThread_IdAndUser_Id ---

    @Test
    void existsByThreadIdAndUserId_returnsTrue_whenExists() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userA, null));

        assertTrue(threadParticipantRepository.existsByThread_IdAndUser_Id(thread.getId(), userA.getId()));
    }

    @Test
    void existsByThreadIdAndUserId_returnsFalse_whenNotExists() {
        assertFalse(threadParticipantRepository.existsByThread_IdAndUser_Id(thread.getId(), userA.getId()));
    }

    // --- markRead ---

    @Test
    void markRead_updatesLastReadAt() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userA, null));
        LocalDateTime readAt = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);

        int updated = threadParticipantRepository.markRead(thread.getId(), userA.getId(), readAt);

        assertEquals(1, updated);
        em.clear();
        Optional<ThreadParticipantEntity> result =
                threadParticipantRepository.findByThread_IdAndUser_Id(thread.getId(), userA.getId());
        assertTrue(result.isPresent());
        assertEquals(readAt, result.get().getLastReadAt());
    }

    @Test
    void markRead_returnsZero_whenParticipantDoesNotExist() {
        int updated = threadParticipantRepository.markRead(thread.getId(), userA.getId(), LocalDateTime.now());

        assertEquals(0, updated);
    }

    // --- findReadStatesForThreads ---

    @Test
    void findReadStatesForThreads_returnsStatesForGivenThreads() {
        LocalDateTime readAt = LocalDateTime.now();
        em.persistFlushFind(new ThreadParticipantEntity(thread, userA, readAt));

        ThreadEntity otherThread = em.persistFlushFind(TestUtilities.validThread(userA));
        em.persistFlushFind(new ThreadParticipantEntity(otherThread, userA, null));

        List<ThreadReadState> result = threadParticipantRepository
                .findReadStatesForThreads(List.of(thread.getId(), otherThread.getId()), userA.getId());

        assertEquals(2, result.size());
    }

    @Test
    void findReadStatesForThreads_returnsEmpty_whenNoMatchingThreads() {
        List<ThreadReadState> result = threadParticipantRepository
                .findReadStatesForThreads(List.of(999L), userA.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findReadStatesForThreads_doesNotReturnOtherUsersStates() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userB, LocalDateTime.now()));

        List<ThreadReadState> result = threadParticipantRepository
                .findReadStatesForThreads(List.of(thread.getId()), userA.getId());

        assertTrue(result.isEmpty());
    }

    // --- existsUnreadForUser ---

    @Test
    void existsUnreadForUser_returnsTrue_whenNewMessageAfterLastRead() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userB, LocalDateTime.now().minusMinutes(10)));
        em.persistFlushFind(TestUtilities.validMessage(userA, userB, "Hello", thread));

        assertTrue(threadParticipantRepository.existsUnreadForUser(userB.getId()));
    }

    @Test
    void existsUnreadForUser_returnsTrue_whenNeverRead() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userB, null));
        em.persistFlushFind(TestUtilities.validMessage(userA, userB, "Hello", thread));

        assertTrue(threadParticipantRepository.existsUnreadForUser(userB.getId()));
    }

    @Test
    void existsUnreadForUser_returnsFalse_whenLastReadAfterLatestMessage() {
        MessageEntity message = em.persistFlushFind(TestUtilities.validMessage(userA, userB, "Hello", thread));
        em.persistFlushFind(new ThreadParticipantEntity(thread, userB, message.getCreatedAt().plusMinutes(1)));

        assertFalse(threadParticipantRepository.existsUnreadForUser(userB.getId()));
    }

    @Test
    void existsUnreadForUser_returnsFalse_whenMessageIsOwnSentMessage() {
        em.persistFlushFind(new ThreadParticipantEntity(thread, userA, null));
        em.persistFlushFind(TestUtilities.validMessage(userA, userB, "Hello", thread));

        assertFalse(threadParticipantRepository.existsUnreadForUser(userA.getId()));
    }

    @Test
    void existsUnreadForUser_returnsFalse_whenNoParticipantRows() {
        assertFalse(threadParticipantRepository.existsUnreadForUser(userA.getId()));
    }

    @Test
    void existsUnreadForUser_ignoresMessagesDeletedByRecipient() {
        MessageEntity message = TestUtilities.validMessage(userA, userB, "Hello", thread);
        message.setDeletedByRecipient(true);
        em.persistFlushFind(message);
        em.persistFlushFind(new ThreadParticipantEntity(thread, userB, null));

        assertFalse(threadParticipantRepository.existsUnreadForUser(userB.getId()));
    }
}
