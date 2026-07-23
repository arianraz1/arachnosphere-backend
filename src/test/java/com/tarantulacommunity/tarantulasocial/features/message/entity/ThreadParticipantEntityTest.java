package com.tarantulacommunity.tarantulasocial.features.message.entity;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ThreadParticipantEntityTest {

    @Test
    void testNoArgsConstructor() {
        ThreadParticipantEntity entity = new ThreadParticipantEntity();
        assertNull(entity.getId());
        assertNull(entity.getThread());
        assertNull(entity.getUser());
        assertNull(entity.getLastReadAt());
    }

    @Test
    void testCustomConstructor_setsIdFromThreadAndUser() {
        ThreadEntity thread = new ThreadEntity();
        thread.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(2L);
        LocalDateTime now = LocalDateTime.now();

        ThreadParticipantEntity entity = new ThreadParticipantEntity(thread, user, now);

        assertEquals(thread, entity.getThread());
        assertEquals(user, entity.getUser());
        assertEquals(now, entity.getLastReadAt());
        assertNotNull(entity.getId());
        assertEquals(1L, entity.getId().getThreadId());
        assertEquals(2L, entity.getId().getUserId());
    }

    @Test
    void testCustomConstructor_allowsNullLastReadAt() {
        ThreadEntity thread = new ThreadEntity();
        thread.setId(1L);
        UserEntity user = new UserEntity();
        user.setId(2L);

        ThreadParticipantEntity entity = new ThreadParticipantEntity(thread, user, null);

        assertNull(entity.getLastReadAt());
    }

    @Test
    void testSettersAndGetters() {
        ThreadParticipantEntity entity = new ThreadParticipantEntity();
        ThreadParticipantId id = new ThreadParticipantId(1L, 2L);
        LocalDateTime now = LocalDateTime.now();

        entity.setId(id);
        entity.setLastReadAt(now);

        assertEquals(id, entity.getId());
        assertEquals(now, entity.getLastReadAt());
    }
}
