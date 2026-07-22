package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired
    private MessageRepository messageRepository;

    private ThreadEntity thread;
    private UserEntity sender;
    private UserEntity recipient;

    @BeforeEach
    void setUp() {
        sender = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        recipient = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        thread = em.persistFlushFind(TestUtilities.validThread(sender));
    }

    // --- findByThreadIdOrderByCreatedAtAsc ---

    @Test
    void findByThreadIdOrderByCreatedAtAsc_returnsMessagesInOrder() {
        MessageEntity first = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        MessageEntity second = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));

        List<MessageEntity> result = messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(first.getId());
        assertThat(result.get(1).getId()).isEqualTo(second.getId());
    }

    @Test
    void findByThreadIdOrderByCreatedAtAsc_returnsEmptyWhenNoMessages() {
        List<MessageEntity> result = messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByThreadIdOrderByCreatedAtAsc_doesNotReturnMessagesFromOtherThreads() {
        ThreadEntity otherThread = em.persistFlushFind(TestUtilities.validThread(sender));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Other", otherThread));
        MessageEntity mine = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Mine", thread));

        List<MessageEntity> result = messageRepository.findByThreadIdOrderByCreatedAtAsc(thread.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(mine.getId());
    }

    // --- findThreadsByUserId ---

    @Test
    void findThreadsByUserId_returnsThreadsForSender() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(thread.getId());
    }

    @Test
    void findThreadsByUserId_returnsThreadsForRecipient() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(recipient.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findThreadsByUserId_doesNotReturnDuplicateThreads() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void findThreadsByUserId_returnsEmptyWhenNoThreads() {
        UserEntity noThreadUser = em.persistFlushFind(TestUtilities.verifiedUser("3", "3"));

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(noThreadUser.getId(), PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findThreadsByUserId_doesNotReturnThreadWhenSenderDeletedAllMessages() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedBySender(true);
        em.persistFlushFind(msg);

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(sender.getId(), PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findThreadsByUserId_doesNotReturnThreadWhenRecipientDeletedAllMessages() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedByRecipient(true);
        em.persistFlushFind(msg);

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(recipient.getId(), PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findThreadsByUserId_stillReturnsThreadForOtherSideWhenOneSideDeletes() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedBySender(true);
        em.persistFlushFind(msg);

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(recipient.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    // --- hasVisibleMessageInThread ---

    @Test
    void hasVisibleMessageInThread_returnsTrueForSenderWithVisibleMessage() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.hasVisibleMessageInThread(thread.getId(), sender.getId());

        assertThat(result).isTrue();
    }

    @Test
    void hasVisibleMessageInThread_returnsTrueForRecipientWithVisibleMessage() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.hasVisibleMessageInThread(thread.getId(), recipient.getId());

        assertThat(result).isTrue();
    }

    @Test
    void hasVisibleMessageInThread_returnsFalseWhenSenderDeletedAllMessages() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedBySender(true);
        em.persistFlushFind(msg);

        boolean result = messageRepository.hasVisibleMessageInThread(thread.getId(), sender.getId());

        assertThat(result).isFalse();
    }

    @Test
    void hasVisibleMessageInThread_returnsFalseWhenRecipientDeletedAllMessages() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedByRecipient(true);
        em.persistFlushFind(msg);

        boolean result = messageRepository.hasVisibleMessageInThread(thread.getId(), recipient.getId());

        assertThat(result).isFalse();
    }

    // --- isUserParticipantInThread ---

    @Test
    void isUserParticipantInThread_returnsTrue_forSenderOrRecipient() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean senderIsParticipant = messageRepository.isUserParticipantInThread(thread.getId(), sender.getId());
        boolean recipientIsParticipant = messageRepository.isUserParticipantInThread(thread.getId(), recipient.getId());

        assertThat(senderIsParticipant).isTrue();
        assertThat(recipientIsParticipant).isTrue();
    }

    @Test
    void isUserParticipantInThread_returnsFalse_forUnrelatedUser() {
        UserEntity stranger = em.persistFlushFind(TestUtilities.verifiedUser("9", "9"));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.isUserParticipantInThread(thread.getId(), stranger.getId());

        assertThat(result).isFalse();
    }

    // --- existsByThreadId ---

    @Test
    void existsByThreadId_returnsTrue_whenMessagesExist() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean exists = messageRepository.existsByThreadId(thread.getId());

        assertThat(exists).isTrue();
    }

    @Test
    void existsByThreadId_returnsFalse_whenNoMessages() {
        boolean exists = messageRepository.existsByThreadId(thread.getId());

        assertThat(exists).isFalse();
    }

    // --- findLatestVisibleMessagePerThread ---

    @Test
    void findLatestVisibleMessagePerThread_returnsLatestVisiblePerThread_forUser() {
        // same thread: create two messages, second is latest
        MessageEntity m1 = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        MessageEntity m2 = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));

        // another thread with one visible message for recipient
        ThreadEntity otherThread = em.persistFlushFind(TestUtilities.validThread(sender));
        MessageEntity otherMsg = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hi", otherThread));

        List<Long> threadIds = List.of(thread.getId(), otherThread.getId());

        // for sender, latest in first thread should be m2, and otherMsg for otherThread
        List<MessageEntity> latestForSender = messageRepository.findLatestVisibleMessagePerThread(threadIds, sender.getId());
        assertThat(latestForSender).hasSize(2);
        // ensure it contains the messages from correct threads
        assertThat(latestForSender.stream().map(e -> e.getThread().getId()).toList())
                .containsExactlyInAnyOrder(thread.getId(), otherThread.getId());

        // Hide m2 from recipient; recipient should then see otherMsg for otherThread and still m2 for first thread if visible to recipient
        m2.setDeletedByRecipient(true);
        em.persistFlushFind(m2);
        List<MessageEntity> latestForRecipient = messageRepository.findLatestVisibleMessagePerThread(threadIds, recipient.getId());
        assertThat(latestForRecipient).hasSize(2);
        assertThat(latestForRecipient.stream().map(e -> e.getThread().getId()).toList())
                .containsExactlyInAnyOrder(thread.getId(), otherThread.getId());
    }
}