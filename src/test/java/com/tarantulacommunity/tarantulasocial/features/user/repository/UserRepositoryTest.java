package com.tarantulacommunity.tarantulasocial.features.user.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsCorrectUser() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        Optional<UserEntity> result = userRepository.findByUsername("test1");

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void findByUsername_returnsEmpty_whenNoMatch() {
        Optional<UserEntity> result = userRepository.findByUsername("nonexistent");

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByEmail_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        assertTrue(userRepository.existsByEmail("test1@example.com"));
    }

    @Test
    void existsByEmail_returnsFalse_whenNoMatch() {
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void existsByUsername_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        assertTrue(userRepository.existsByUsername("test1"));
    }

    @Test
    void existsByUsername_returnsFalse_whenNoMatch() {
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }
}
