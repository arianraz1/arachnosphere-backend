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

import java.time.LocalDateTime;
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

    // --- findVisibleMessagesByThreadId ---

    @Test
    void findVisibleMessagesByThreadId_returnsVisibleMessagesInDescendingOrder() {
        MessageEntity first = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        MessageEntity second = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getId()).isEqualTo(second.getId());
        assertThat(result.getContent().get(1).getId()).isEqualTo(first.getId());
    }

    @Test
    void findVisibleMessagesByThreadId_returnsEmptyWhenNoMessages() {
        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 10));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void findVisibleMessagesByThreadId_doesNotReturnMessagesFromOtherThreads() {
        ThreadEntity otherThread = em.persistFlushFind(TestUtilities.validThread(sender));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Other", otherThread));
        MessageEntity mine = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Mine", thread));

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(mine.getId());
    }

    @Test
    void findVisibleMessagesByThreadId_filtersSenderDeletedMessages() {
        MessageEntity visible = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Visible", thread));
        MessageEntity deleted = TestUtilities.validMessage(sender, recipient, "Deleted", thread);
        deleted.setDeletedBySender(true);
        em.persistFlushFind(deleted);

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(visible.getId());
    }

    @Test
    void findVisibleMessagesByThreadId_filtersRecipientDeletedMessages() {
        MessageEntity visible = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Visible", thread));
        MessageEntity deleted = TestUtilities.validMessage(sender, recipient, "Deleted", thread);
        deleted.setDeletedByRecipient(true);
        em.persistFlushFind(deleted);

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), recipient.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(visible.getId());
    }

    @Test
    void findVisibleMessagesByThreadId_doesNotFilterMessageDeletedByOtherSide() {
        MessageEntity deletedBySender = TestUtilities.validMessage(sender, recipient, "Deleted by sender", thread);
        deletedBySender.setDeletedBySender(true);
        em.persistFlushFind(deletedBySender);

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), recipient.getId(), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(deletedBySender.getId());
    }

    @Test
    void findVisibleMessagesByThreadId_respectsPageSize() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Third", thread));

        Page<MessageEntity> result = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 2));

        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    void findVisibleMessagesByThreadId_respectsPageNumber() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Third", thread));

        Page<MessageEntity> page0 = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 2));
        Page<MessageEntity> page1 = messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(1, 2));

        assertThat(page0.getContent()).hasSize(2);
        assertThat(page1.getContent()).hasSize(1);
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

    // --- markDeletedBySenderForThread ---

    @Test
    void markDeletedBySenderForThread_marksOnlySenderMessages() {
        MessageEntity senderMsg = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "From sender", thread));
        MessageEntity recipientMsg = em.persistFlushFind(TestUtilities.validMessage(recipient, sender, "From recipient", thread));

        int updated = messageRepository.markDeletedBySenderForThread(thread.getId(), sender.getId());

        assertThat(updated).isEqualTo(1);
        em.clear();
        MessageEntity updatedSenderMsg = em.find(MessageEntity.class, senderMsg.getId());
        MessageEntity updatedRecipientMsg = em.find(MessageEntity.class, recipientMsg.getId());
        assertThat(updatedSenderMsg.isDeletedBySender()).isTrue();
        assertThat(updatedRecipientMsg.isDeletedBySender()).isFalse();
    }

    @Test
    void markDeletedBySenderForThread_doesNotMarkAlreadyDeleted() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedBySender(true);
        em.persistFlushFind(msg);

        int updated = messageRepository.markDeletedBySenderForThread(thread.getId(), sender.getId());

        assertThat(updated).isEqualTo(0);
    }

    // --- markDeletedByRecipientForThread ---

    @Test
    void markDeletedByRecipientForThread_marksOnlyRecipientMessages() {
        MessageEntity senderMsg = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "From sender", thread));
        MessageEntity recipientMsg = em.persistFlushFind(TestUtilities.validMessage(recipient, sender, "From recipient", thread));

        int updated = messageRepository.markDeletedByRecipientForThread(thread.getId(), sender.getId());

        assertThat(updated).isEqualTo(1);
        em.clear();
        MessageEntity updatedSenderMsg = em.find(MessageEntity.class, senderMsg.getId());
        MessageEntity updatedRecipientMsg = em.find(MessageEntity.class, recipientMsg.getId());
        assertThat(updatedSenderMsg.isDeletedByRecipient()).isFalse();
        assertThat(updatedRecipientMsg.isDeletedByRecipient()).isTrue();
    }

    @Test
    void markDeletedByRecipientForThread_doesNotMarkAlreadyDeleted() {
        MessageEntity msg = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        msg.setDeletedByRecipient(true);
        em.persistFlushFind(msg);

        int updated = messageRepository.markDeletedByRecipientForThread(thread.getId(), recipient.getId());

        assertThat(updated).isEqualTo(0);
    }

    // --- deleteFullyDeletedMessagesInThread ---

    @Test
    void deleteFullyDeletedMessagesInThread_deletesOnlyFullyDeletedMessages() {
        MessageEntity fullyDeleted = TestUtilities.validMessage(sender, recipient, "Fully deleted", thread);
        fullyDeleted.setDeletedBySender(true);
        fullyDeleted.setDeletedByRecipient(true);
        em.persistFlushFind(fullyDeleted);

        MessageEntity partiallyDeleted = TestUtilities.validMessage(sender, recipient, "Partially deleted", thread);
        partiallyDeleted.setDeletedBySender(true);
        em.persistFlushFind(partiallyDeleted);

        MessageEntity notDeleted = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Not deleted", thread));

        int deleted = messageRepository.deleteFullyDeletedMessagesInThread(thread.getId());

        assertThat(deleted).isEqualTo(1);
        em.clear();
        assertThat(em.find(MessageEntity.class, fullyDeleted.getId())).isNull();
        assertThat(em.find(MessageEntity.class, partiallyDeleted.getId())).isNotNull();
        assertThat(em.find(MessageEntity.class, notDeleted.getId())).isNotNull();
    }

    @Test
    void deleteFullyDeletedMessagesInThread_deletesNothingWhenNoFullyDeleted() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hello", thread));

        int deleted = messageRepository.deleteFullyDeletedMessagesInThread(thread.getId());

        assertThat(deleted).isEqualTo(0);
        assertThat(messageRepository.findVisibleMessagesByThreadId(thread.getId(), sender.getId(), PageRequest.of(0, 10)).getTotalElements()).isEqualTo(1);
    }

    // --- findLatestMessageTimestamp ---

    @Test
    void findLatestMessageTimestamp_returnsMostRecentCreatedAt() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First", thread));
        MessageEntity second = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second", thread));

        LocalDateTime result = messageRepository.findLatestMessageTimestamp(thread.getId(), sender.getId());

        assertThat(result).isEqualTo(second.getCreatedAt());
    }

    @Test
    void findLatestMessageTimestamp_returnsNull_whenNoMessages() {
        LocalDateTime result = messageRepository.findLatestMessageTimestamp(thread.getId(), sender.getId());

        assertThat(result).isNull();
    }

    @Test
    void findLatestMessageTimestamp_excludesMessagesDeletedBySender() {
        MessageEntity visible = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Visible", thread));
        MessageEntity deleted = TestUtilities.validMessage(sender, recipient, "Deleted", thread);
        deleted.setDeletedBySender(true);
        em.persistFlushFind(deleted);

        LocalDateTime result = messageRepository.findLatestMessageTimestamp(thread.getId(), sender.getId());

        assertThat(result).isEqualTo(visible.getCreatedAt());
    }

    @Test
    void findLatestMessageTimestamp_excludesMessagesDeletedByRecipient() {
        MessageEntity visible = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Visible", thread));
        MessageEntity deleted = TestUtilities.validMessage(sender, recipient, "Deleted", thread);
        deleted.setDeletedByRecipient(true);
        em.persistFlushFind(deleted);

        LocalDateTime result = messageRepository.findLatestMessageTimestamp(thread.getId(), recipient.getId());

        assertThat(result).isEqualTo(visible.getCreatedAt());
    }

    @Test
    void findLatestMessageTimestamp_scopedToGivenThread() {
        em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "In thread", thread));

        ThreadEntity otherThread = em.persistFlushFind(TestUtilities.validThread(sender));
        MessageEntity otherMsg = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Other", otherThread));

        LocalDateTime result = messageRepository.findLatestMessageTimestamp(otherThread.getId(), sender.getId());

        assertThat(result).isEqualTo(otherMsg.getCreatedAt());
    }
}