package com.tarantulacommunity.tarantulasocial.features.message.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@DataJpaTest
class ThreadRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ThreadRepository threadRepository;

    @Test
    void save_persistsThread() {
        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        Optional<ThreadEntity> result = threadRepository.findById(thread.getId());

        assertTrue(result.isPresent());
        assertEquals(thread.getId(), result.get().getId());
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        Optional<ThreadEntity> result = threadRepository.findById(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void delete_removesThread() {
        UserEntity user = em.persistFlushFind(TestUtilities.validUser("2", "2"));
        ThreadEntity thread = em.persistFlushFind(TestUtilities.validThread(user));

        threadRepository.delete(thread);

        Optional<ThreadEntity> result = threadRepository.findById(thread.getId());
        assertTrue(result.isEmpty());
    }
}