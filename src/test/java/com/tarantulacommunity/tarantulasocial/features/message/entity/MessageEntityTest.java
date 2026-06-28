package com.tarantulacommunity.tarantulasocial.features.message.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
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

    @Test
    void validMessagePersistsWithTimestampAndDefaults() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("2", "2"));

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hey, nice GBB!");
        MessageEntity saved = em.persistFlushFind(message);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("Hey, nice GBB!", saved.getContent());
        assertFalse(saved.isDeletedBySender());
        assertFalse(saved.isDeletedByRecipient());
    }

    @Test
    void blankContentFailsBeanValidation() {
        UserEntity sender = TestUtilities.validUser("3", "3");
        UserEntity recipient = TestUtilities.validUser("4", "4");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "");

        Set<ConstraintViolation<MessageEntity>> violations = validator.validate(message);
        assertEquals(1, violations.size());
    }

    @Test
    void nullSenderFailsBeanValidationOrConstraint() {
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("5", "5"));

        MessageEntity message = new MessageEntity();
        message.setRecipient(recipient);
        message.setContent("Hello");
        // sender left null

        assertThrows(Exception.class, () -> em.persistAndFlush(message));
    }

    @Test
    void nullRecipientFailsBeanValidationOrConstraint() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("6", "6"));

        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setContent("Hello");
        // recipient left null

        assertThrows(Exception.class, () -> em.persistAndFlush(message));
    }

    @Test
    void deletedFlagsCanBeUpdatedIndependently() {
        UserEntity sender = em.persistFlushFind(TestUtilities.validUser("7", "7"));
        UserEntity recipient = em.persistFlushFind(TestUtilities.validUser("8", "8"));

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Test message");
        MessageEntity saved = em.persistFlushFind(message);

        saved.setDeletedBySender(true);
        MessageEntity updated = em.persistFlushFind(saved);

        assertTrue(updated.isDeletedBySender());
        assertFalse(updated.isDeletedByRecipient());
    }

    @Test
    void contentTooLongFailsBeanValidation() {
        UserEntity sender = TestUtilities.validUser("9", "9");
        UserEntity recipient = TestUtilities.validUser("10", "10");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "a".repeat(2001));

        Set<ConstraintViolation<MessageEntity>> violations = validator.validate(message);
        assertEquals(1, violations.size());
    }
}