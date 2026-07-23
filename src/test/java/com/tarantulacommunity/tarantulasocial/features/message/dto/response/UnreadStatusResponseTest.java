package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnreadStatusResponseTest {

    @Test
    void testUnreadTrue() {
        UnreadStatusResponse response = new UnreadStatusResponse(true);
        assertTrue(response.unread());
    }

    @Test
    void testUnreadFalse() {
        UnreadStatusResponse response = new UnreadStatusResponse(false);
        assertFalse(response.unread());
    }

    @Test
    void testEquality() {
        UnreadStatusResponse a = new UnreadStatusResponse(true);
        UnreadStatusResponse b = new UnreadStatusResponse(true);
        assertEquals(a, b);
    }

    @Test
    void testInequality() {
        UnreadStatusResponse a = new UnreadStatusResponse(true);
        UnreadStatusResponse b = new UnreadStatusResponse(false);
        assertNotEquals(a, b);
    }
}
