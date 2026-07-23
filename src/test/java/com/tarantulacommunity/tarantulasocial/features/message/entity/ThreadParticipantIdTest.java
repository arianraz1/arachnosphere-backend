package com.tarantulacommunity.tarantulasocial.features.message.entity;

import org.junit.jupiter.api.Test;

import java.io.Serializable;

import static org.junit.jupiter.api.Assertions.*;

class ThreadParticipantIdTest {

    @Test
    void testNoArgsConstructor() {
        ThreadParticipantId id = new ThreadParticipantId();
        assertNull(id.getThreadId());
        assertNull(id.getUserId());
    }

    @Test
    void testAllArgsConstructor() {
        ThreadParticipantId id = new ThreadParticipantId(1L, 2L);
        assertEquals(1L, id.getThreadId());
        assertEquals(2L, id.getUserId());
    }

    @Test
    void testSettersAndGetters() {
        ThreadParticipantId id = new ThreadParticipantId();
        id.setThreadId(1L);
        id.setUserId(2L);

        assertEquals(1L, id.getThreadId());
        assertEquals(2L, id.getUserId());
    }

    @Test
    void testEquals_sameValues() {
        ThreadParticipantId id1 = new ThreadParticipantId(1L, 2L);
        ThreadParticipantId id2 = new ThreadParticipantId(1L, 2L);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testEquals_differentValues() {
        ThreadParticipantId id1 = new ThreadParticipantId(1L, 2L);
        ThreadParticipantId id2 = new ThreadParticipantId(1L, 3L);

        assertNotEquals(id1, id2);
    }

    @Test
    void testSerializable() {
        assertTrue(Serializable.class.isAssignableFrom(ThreadParticipantId.class));
    }
}
