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
class PasswordResetTokenEntityTest {

    @Autowired
    private TestEntityManager em;

    @Test
    void validTokenPersistsWithDefaults() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setToken("test-token-123");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        PasswordResetTokenEntity saved = em.persistFlushFind(token);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertEquals("test-token-123", saved.getToken());
        assertEquals(user.getId(), saved.getUser().getId());
    }

    @Test
    void nullUserFailsConstraint() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setToken("test-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void nullTokenFailsConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void nullExpiresAtFailsConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("3", "3"));

        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        token.setUser(user);
        token.setToken("test-token");

        assertThrows(Exception.class, () -> em.persistAndFlush(token));
    }

    @Test
    void duplicateTokenFailsUniqueConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("4", "4"));

        PasswordResetTokenEntity token1 = new PasswordResetTokenEntity();
        token1.setUser(user);
        token1.setToken("duplicate-token");
        token1.setExpiresAt(LocalDateTime.now().plusHours(1));
        em.persistAndFlush(token1);

        PasswordResetTokenEntity token2 = new PasswordResetTokenEntity();
        token2.setUser(user);
        token2.setToken("duplicate-token");
        token2.setExpiresAt(LocalDateTime.now().plusHours(1));

        assertThrows(Exception.class, () -> em.persistAndFlush(token2));
    }
}