package com.tarantulacommunity.tarantulasocial.security.features.password.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.entity.PasswordResetTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.features.password.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class PasswordResetTokenRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
    }

    private PasswordResetTokenEntity persistToken(String token, LocalDateTime expiresAt) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity();
        entity.setUser(user);
        entity.setToken(token);
        entity.setExpiresAt(expiresAt);
        return em.persistFlushFind(entity);
    }

    @Test
    void findByToken_returnsToken_whenExists() {
        persistToken("valid-token", LocalDateTime.now().plusHours(1));

        Optional<PasswordResetTokenEntity> result = tokenRepository.findByToken("valid-token");

        assertTrue(result.isPresent());
        assertEquals("valid-token", result.get().getToken());
    }

    @Test
    void findByToken_returnsEmpty_whenNotExists() {
        Optional<PasswordResetTokenEntity> result = tokenRepository.findByToken("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByUserId_deletesAllTokensForUser() {
        persistToken("token-1", LocalDateTime.now().plusHours(1));
        persistToken("token-2", LocalDateTime.now().plusHours(1));

        tokenRepository.deleteByUserId(user.getId());

        assertTrue(tokenRepository.findByToken("token-1").isEmpty());
        assertTrue(tokenRepository.findByToken("token-2").isEmpty());
    }

    @Test
    void deleteByUserId_doesNotDeleteTokensForOtherUsers() {
        UserEntity otherUser = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));

        PasswordResetTokenEntity otherToken = new PasswordResetTokenEntity();
        otherToken.setUser(otherUser);
        otherToken.setToken("other-token");
        otherToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        em.persistFlushFind(otherToken);

        persistToken("my-token", LocalDateTime.now().plusHours(1));

        tokenRepository.deleteByUserId(user.getId());

        assertTrue(tokenRepository.findByToken("other-token").isPresent());
    }

    @Test
    void findByUserId_returnsToken_whenExists() {
        persistToken("user-token", LocalDateTime.now().plusHours(1));

        Optional<PasswordResetTokenEntity> result = tokenRepository.findByUserId(user.getId());

        assertTrue(result.isPresent());
        assertEquals("user-token", result.get().getToken());
    }

    @Test
    void findByUserId_returnsEmpty_whenNoToken() {
        Optional<PasswordResetTokenEntity> result = tokenRepository.findByUserId(user.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserId_returnsEmpty_whenUserHasNoToken() {
        UserEntity otherUser = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));

        persistToken("my-token", LocalDateTime.now().plusHours(1));

        Optional<PasswordResetTokenEntity> result = tokenRepository.findByUserId(otherUser.getId());

        assertTrue(result.isEmpty());
    }
}
