package com.tarantulacommunity.tarantulasocial.features.user.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.EmailVerificationTokenEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class EmailVerificationTokenRepositoryTest {

    @Autowired
    private TestEntityManager em;
    @Autowired private EmailVerificationTokenRepository tokenRepository;

    private UserEntity persistUser(String suffix) {
        UserEntity user = TestUtilities.validUser(suffix, suffix);
        return em.persistFlushFind(user);
    }

    @Test
    void findByToken_returnsMatchingToken() {
        UserEntity user = persistUser("t1");
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("unique-token-1");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        em.persistFlushFind(token);

        Optional<EmailVerificationTokenEntity> found = tokenRepository.findByToken("unique-token-1");

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByToken_missingToken_returnsEmpty() {
        assertThat(tokenRepository.findByToken("does-not-exist")).isEmpty();
    }

    @Test
    void findByUserId_returnsTokenForUser() {
        UserEntity user = persistUser("t2");
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("unique-token-2");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        em.persistFlushFind(token);

        assertThat(tokenRepository.findByUserId(user.getId())).isPresent();
    }

    @Test
    void deleteByUserId_removesToken() {
        UserEntity user = persistUser("t3");
        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("unique-token-3");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));
        em.persistFlushFind(token);

        tokenRepository.deleteByUserId(user.getId());
        em.flush();

        assertThat(tokenRepository.findByUserId(user.getId())).isEmpty();
    }

    @Test
    void duplicateToken_violatesUniqueConstraint() {
        UserEntity user1 = persistUser("t4");
        UserEntity user2 = persistUser("t5");

        EmailVerificationTokenEntity token1 = new EmailVerificationTokenEntity();
        token1.setUser(user1);
        token1.setToken("shared-token");
        token1.setExpiresAt(LocalDateTime.now().plusHours(24));
        em.persistAndFlush(token1);

        EmailVerificationTokenEntity token2 = new EmailVerificationTokenEntity();
        token2.setUser(user2);
        token2.setToken("shared-token");
        token2.setExpiresAt(LocalDateTime.now().plusHours(24));

        assertThrows(Exception.class, () -> em.persistAndFlush(token2));
    }

    @Test
    void deletingUser_cascadesToToken() {
        UserEntity user = persistUser("t6");

        EmailVerificationTokenEntity token = new EmailVerificationTokenEntity();
        token.setUser(user);
        token.setToken("cascade-token");
        token.setExpiresAt(LocalDateTime.now().plusHours(24));;

        em.persistFlushFind(token);

        em.flush();
        em.clear();

        UserEntity managedUser = em.find(UserEntity.class, user.getId());
        em.remove(managedUser);
        em.flush();

        assertThat(tokenRepository.findByToken("cascade-token")).isEmpty();
    }
}
