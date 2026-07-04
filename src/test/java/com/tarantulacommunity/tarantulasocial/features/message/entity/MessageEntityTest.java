package com.tarantulacommunity.tarantulasocial.features.message.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MessageEntityTest {

    @Autowired
    private TestEntityManager em;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private UserEntity sender;
    private UserEntity recipient;
    private ThreadEntity thread;

    @BeforeEach
    void setUp() {
        sender = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        recipient = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        thread = em.persistFlushFind(TestUtilities.validThread(sender));
    }

    @Test
    void validMessagePersistsWithTimestampAndDefaults() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hey, nice GBB!", thread);
        MessageEntity saved = em.persistFlushFind(message);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Hey, nice GBB!", saved.getContent());
        assertFalse(saved.isDeletedBySender());
        assertFalse(saved.isDeletedByRecipient());
    }

    @Test
    void blankContentFailsBeanValidation() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "", thread);

        Set<ConstraintViolation<MessageEntity>> violations = validator.validate(message);
        assertEquals(1, violations.size());
    }

    @Test
    void nullSenderFailsConstraint() {
        MessageEntity message = new MessageEntity();
        message.setRecipient(recipient);
        message.setContent("Hello");
        message.setThread(thread);

        assertThrows(Exception.class, () -> em.persistAndFlush(message));
    }

    @Test
    void nullRecipientFailsConstraint() {
        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setContent("Hello");
        message.setThread(thread);

        assertThrows(Exception.class, () -> em.persistAndFlush(message));
    }

    @Test
    void deletedFlagsCanBeUpdatedIndependently() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Test message", thread);
        MessageEntity saved = em.persistFlushFind(message);

        saved.setDeletedBySender(true);
        MessageEntity updated = em.persistFlushFind(saved);

        assertTrue(updated.isDeletedBySender());
        assertFalse(updated.isDeletedByRecipient());
    }

    @Test
    void contentTooLongFailsBeanValidation() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "a".repeat(2001), thread);

        Set<ConstraintViolation<MessageEntity>> violations = validator.validate(message);
        assertEquals(1, violations.size());
    }

    @Test
    void nullThreadFailsConstraint() {
        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("Hello");

        assertThrows(Exception.class, () -> em.persistAndFlush(message));
    }

    @Test
    void messageCorrectlyAssociatedWithThread() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hello", thread);
        MessageEntity saved = em.persistFlushFind(message);

        assertNotNull(saved.getThread());
        assertEquals(thread.getId(), saved.getThread().getId());
    }

    @Test
    void bothDeletedFlagsCanBeSetToTrue() {
        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Test", thread);
        MessageEntity saved = em.persistFlushFind(message);

        saved.setDeletedBySender(true);
        saved.setDeletedByRecipient(true);
        MessageEntity updated = em.persistFlushFind(saved);

        assertTrue(updated.isDeletedBySender());
        assertTrue(updated.isDeletedByRecipient());
    }
}