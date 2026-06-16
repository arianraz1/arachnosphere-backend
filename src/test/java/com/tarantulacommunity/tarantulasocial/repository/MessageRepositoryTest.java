package com.tarantulacommunity.tarantulasocial.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.domain.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.domain.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class MessageRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private MessageRepository messageRepository;

    @Test
    void findByRecipientAndDeletedByRecipientFalse_returnsMessages() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity message = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hey!"));

        Page<MessageEntity> result = messageRepository
                .findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(recipient, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(message, result.getContent().getFirst());
    }

    @Test
    void findByRecipientAndDeletedByRecipientFalse_doesNotReturnDeletedMessages() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hey!");
        message.setDeletedByRecipient(true);
        em.persistFlushFind(message);

        Page<MessageEntity> result = messageRepository
                .findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(recipient, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRecipientAndDeletedByRecipientFalse_doesNotReturnOtherUsersMessages() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient1 = em.persistFlushFind(TestUtilities.validUser("2", "2"));
        UserEntity recipient2 = em.persistFlushFind(TestUtilities.validUser("3", "3"));

        em.persistFlushFind(TestUtilities.validMessage(sender, recipient2, "Hey!"));

        Page<MessageEntity> result = messageRepository
                .findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(recipient1, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByRecipientAndDeletedByRecipientFalse_returnsInChronologicalOrder() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity first = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First"));
        MessageEntity second = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second"));

        Page<MessageEntity> result = messageRepository
                .findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(recipient, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(second, result.getContent().get(0));
        assertEquals(first, result.getContent().get(1));
    }

    @Test
    void findBySenderAndDeletedBySenderFalse_returnsMessages() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity message = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Hey!"));

        Page<MessageEntity> result = messageRepository
                .findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(sender, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(message, result.getContent().getFirst());
    }

    @Test
    void findBySenderAndDeletedBySenderFalse_doesNotReturnDeletedMessages() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hey!");
        message.setDeletedBySender(true);
        em.persistFlushFind(message);

        Page<MessageEntity> result = messageRepository
                .findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(sender, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findBySenderAndDeletedBySenderFalse_doesNotReturnOtherUsersSentMessages() {
        UserEntity sender1 = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity sender2 = em.persistFlushFind(TestUtilities.validUser("2", "2"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("3", "3"));

        em.persistFlushFind(TestUtilities.validMessage(sender2, recipient, "Hey!"));

        Page<MessageEntity> result = messageRepository
                .findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(sender1, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findBySenderAndDeletedBySenderFalse_returnsInChronologicalOrder() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity first = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "First"));
        MessageEntity second = em.persistFlushFind(TestUtilities.validMessage(sender, recipient, "Second"));

        Page<MessageEntity> result = messageRepository
                .findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(sender, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(second, result.getContent().get(0));
        assertEquals(first, result.getContent().get(1));
    }
}
