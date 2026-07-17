package com.tarantulacommunity.tarantulasocial.features.message.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ThreadEntityTest {

    @Autowired
    private TestEntityManager em;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validThreadPersistsWithDefaults() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        assertNotNull(thread.getId());
        assertNotNull(thread.getCreatedAt());
        assertNotNull(thread.getUpdatedAt());
        assertTrue(thread.isActive());
        assertEquals("Test subject", thread.getSubject());
        assertEquals(user.getId(), thread.getCreatedBy().getId());
    }

    @Test
    void nullCreatedByFailsConstraint() {
        ThreadEntity thread = new ThreadEntity();
        thread.setSubject("Test");

        assertThrows(Exception.class, () -> em.persistAndFlush(thread));
    }

    @Test
    void subjectOver255CharsFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("2", "2");
        ThreadEntity thread = new ThreadEntity();
        thread.setCreatedBy(user);
        thread.setSubject("a".repeat(256));

        Set<ConstraintViolation<ThreadEntity>> violations = validator.validate(thread);
        assertEquals(1, violations.size());
    }

    @Test
    void nullSubjectIsAllowed() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("3", "3"));
        ThreadEntity thread = new ThreadEntity();
        thread.setCreatedBy(user);
        thread.setSubject(null);

        ThreadEntity saved = em.persistFlushFind(thread);
        assertNull(saved.getSubject());
    }

    @Test
    void activeDefaultsToTrue() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("4", "4"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        assertTrue(thread.isActive());
    }

    @Test
    void activeCanBeSetToFalse() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("5", "5"));
        ThreadEntity thread = TestUtilities.validThread(user);
        thread.setActive(false);
        ThreadEntity saved = em.persistFlushFind(thread);

        assertFalse(saved.isActive());
    }

    @Test
    void createdAtIsNotUpdatable() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("6", "6"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        LocalDateTime originalCreatedAt = thread.getCreatedAt();
        thread.setSubject("Updated subject");
        ThreadEntity updated = em.persistFlushFind(thread);

        assertEquals(originalCreatedAt, updated.getCreatedAt());
    }

    @Test
    void updatedAtChangesOnUpdate() throws InterruptedException {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("7", "7"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        LocalDateTime originalUpdatedAt = thread.getUpdatedAt();

        Thread.sleep(100);

        thread.setUpdatedAt(LocalDateTime.now());
        em.persistFlushFind(thread);

        em.clear();
        ThreadEntity updated = em.find(ThreadEntity.class, thread.getId());

        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt));
    }


    @Test
    void messagesListIsEmptyByDefault() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("8", "8"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        assertNotNull(thread.getMessages());
        assertTrue(thread.getMessages().isEmpty());
    }
}