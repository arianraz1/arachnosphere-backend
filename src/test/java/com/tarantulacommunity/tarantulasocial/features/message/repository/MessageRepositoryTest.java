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

    @Autowired private TestEntityManager em;
    @Autowired private MessageRepository messageRepository;

    private ThreadEntity thread;
    private UserEntity sender;
    private UserEntity recipient;

    @BeforeEach
    void setUp() {
        sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));
        thread = em.persistFlushFind(TestUtilities.validThread(sender));
    }

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
        UserEntity noThreadUser = em.persistFlushFind(TestUtilities.validUser("3", "3"));

        Page<ThreadEntity> result = messageRepository.findThreadsByUserId(noThreadUser.getId(), PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void existsByThreadIdAndUserId_returnsTrueForSender() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.existsByThreadIdAndUserId(thread.getId(), sender.getId());

        assertThat(result).isTrue();
    }

    @Test
    void existsByThreadIdAndUserId_returnsTrueForRecipient() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.existsByThreadIdAndUserId(thread.getId(), recipient.getId());

        assertThat(result).isTrue();
    }

    @Test
    void existsByThreadIdAndUserId_returnsFalseForUnrelatedUser() {
        UserEntity other = em.persistFlushFind(TestUtilities.validUser("3", "3"));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        boolean result = messageRepository.existsByThreadIdAndUserId(thread.getId(), other.getId());

        assertThat(result).isFalse();
    }

    @Test
    void findLatestMessagePerThread_returnsLatestMessageForEachThread() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        MessageEntity latest = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Latest", thread));

        List<MessageEntity> result = messageRepository.findLatestMessagePerThread(List.of(thread.getId()));

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(latest.getId());
    }

    @Test
    void findLatestMessagePerThread_returnsOnePerThread() {
        ThreadEntity thread2 = em.persistFlushFind(TestUtilities.validThread(sender));
        MessageEntity latest1 = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "T1 latest", thread));
        MessageEntity latest2 = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "T2 latest", thread2));

        List<MessageEntity> result = messageRepository.findLatestMessagePerThread(
                List.of(thread.getId(), thread2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result.stream().map(MessageEntity::getId))
                .containsExactlyInAnyOrder(latest1.getId(), latest2.getId());
    }

    @Test
    void findLatestMessagePerThread_returnsEmptyWhenNoThreadIds() {
        List<MessageEntity> result = messageRepository.findLatestMessagePerThread(List.of());

        assertThat(result).isEmpty();
    }
}