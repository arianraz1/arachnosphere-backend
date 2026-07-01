package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ThreadSummaryResponseTest {

    @Test
    void testNoArgsConstructor() {
        ThreadSummaryResponse response = new ThreadSummaryResponse();

        assertNotNull(response);
        assertNull(response.getThreadId());
        assertNull(response.getSubject());
        assertNull(response.getOtherParticipantUsername());
        assertNull(response.getOtherParticipantId());
        assertNull(response.getLatestMessagePreview());
        assertNull(response.getLatestMessageTimestamp());
        assertFalse(response.isActive());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();

        ThreadSummaryResponse response = new ThreadSummaryResponse(
                1L, "Subject", "user2", 2L, "Hello...", timestamp, true
        );

        assertEquals(1L, response.getThreadId());
        assertEquals("Subject", response.getSubject());
        assertEquals("user2", response.getOtherParticipantUsername());
        assertEquals(2L, response.getOtherParticipantId());
        assertEquals("Hello...", response.getLatestMessagePreview());
        assertEquals(timestamp, response.getLatestMessageTimestamp());
        assertTrue(response.isActive());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime timestamp = LocalDateTime.now();
        ThreadSummaryResponse response = new ThreadSummaryResponse();

        response.setThreadId(1L);
        response.setSubject("Subject");
        response.setOtherParticipantUsername("user2");
        response.setOtherParticipantId(2L);
        response.setLatestMessagePreview("Hello...");
        response.setLatestMessageTimestamp(timestamp);
        response.setActive(true);

        assertEquals(1L, response.getThreadId());
        assertEquals("Subject", response.getSubject());
        assertEquals("user2", response.getOtherParticipantUsername());
        assertEquals(2L, response.getOtherParticipantId());
        assertEquals("Hello...", response.getLatestMessagePreview());
        assertEquals(timestamp, response.getLatestMessageTimestamp());
        assertTrue(response.isActive());
    }

    @Test
    void testActiveDefaultsFalseWithNoArgsConstructor() {
        ThreadSummaryResponse response = new ThreadSummaryResponse();
        assertFalse(response.isActive());
    }

    @Test
    void testNullTimestamp() {
        ThreadSummaryResponse response = new ThreadSummaryResponse(
                1L, "Subject", "user2", 2L, "Hello...", null, true
        );
        assertNull(response.getLatestMessageTimestamp());
    }

    @Test
    void testNullSubject() {
        ThreadSummaryResponse response = new ThreadSummaryResponse(
                1L, null, "user2", 2L, "Hello...", LocalDateTime.now(), true
        );
        assertNull(response.getSubject());
    }
}
