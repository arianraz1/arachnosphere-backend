package com.tarantulacommunity.tarantulasocial.security.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmailVerificationTokenEntityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void validTokenPersistsWithDefaults() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("1", "1"));

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("test-token-123");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        EmailVerificationTokenEntity saved = em.persistFlushFind(token);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("test-token-123", saved.getToken());
        assertEquals(user.getId(), saved.getUser().getId());
    }

    @Test
    void nullUserFailsConstraint() {
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setToken("test-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void nullTokenFailsConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("2", "2"));

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void nullExpiresAtFailsConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("3", "3"));

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("test-token");

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void duplicateTokenFailsUniqueConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("4", "4"));

        EmailVerificationTokenEntity token1 = new EmailVerificationTokenEntity();
        token1.setUser(user);
        token1.setToken("duplicate-token");
        token1.setExpiresAt(LocalDateTime.now().plusHours(24));
        em.persistAndFlush(token1);

        EmailVerificationTokenEntity token2 = new EmailVerificationTokenEntity();
        token2.setUser(user);
        token2.setToken("duplicate-token");
        token2.setExpiresAt(LocalDateTime.now().plusHours(24));

        assertThrows(Exception.class, () -> em.persistAndFlush(token2));
    }

    @Test
    void createdAtIsNotUpdatable() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("5", "5"));

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("test-token-5");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));

        EmailVerificationTokenEntity saved = em.persistFlushFind(token);
        LocalDateTime originalCreatedAt = saved.getCreatedAt();

        saved.setExpiresAt(LocalDateTime.now().plusHours(48));
        EmailVerificationTokenEntity updated = em.persistFlushFind(saved);

        assertEquals(originalCreatedAt, updated.getCreatedAt());
    }

    @Test
    void tokenDeletedWhenUserDeleted() {
        UserEntity user = em.persistFlushFind(TestUtilities.unverifiedUser("6", "6"));

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("test-token-6");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        EmailVerificationTokenEntity saved = em.persistFlushFind(token);
        Long savedId = saved.getId();

        em.remove(em.merge(token));
        em.remove(em.merge(user));
        em.flush();

        assertNull(em.find(EmailVerificationTokenEntity.class, savedId));
    }
}