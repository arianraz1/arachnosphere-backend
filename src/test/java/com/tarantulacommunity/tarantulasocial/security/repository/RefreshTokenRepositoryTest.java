package com.tarantulacommunity.tarantulasocial.security.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.entity.PasswordResetTokenEntity;
import com.tarantulacommunity.tarantulasocial.security.entity.RefreshTokenEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class RefreshTokenRepositoryTest {

    @Autowired private RefreshTokenRepository tokenRepository;
    @Autowired private UserRepository userRepository;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("repo@test.com");
        user.setUsername("repo");
        user.setPassword("encoded");
        user = userRepository.save(user);
    }

    @Test
    void shouldFindByTokenHash() {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUser(user);
        token.setTokenHash("abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890");
        token.setExpiresAt(Instant.now().plusSeconds(60));
        tokenRepository.save(token);

        Optional<RefreshTokenEntity> found = tokenRepository.findByTokenHash(token.getTokenHash());
        assertThat(found).isPresent();
    }

    @Test
    void shouldDeleteAllByUser() {
        RefreshTokenEntity t1 = createToken("hash1");
        RefreshTokenEntity t2 = createToken("hash2");
        tokenRepository.saveAll(java.util.List.of(t1, t2));

        tokenRepository.deleteAllByUser(user);
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    @Test
    void shouldDeleteExpired() {
        RefreshTokenEntity expired = createToken("hashExpired");
        expired.setExpiresAt(Instant.now().minusSeconds(10));
        tokenRepository.save(expired);

        tokenRepository.deleteAllExpired(Instant.now());
        assertThat(tokenRepository.findAll()).isEmpty();
    }

    private RefreshTokenEntity createToken(String hash) {
        RefreshTokenEntity token = new RefreshTokenEntity();
        token.setUser(user);
        token.setTokenHash(hash);
        token.setExpiresAt(Instant.now().plusSeconds(3600));
        return token;
    }
}
