package com.tarantulacommunity.tarantulasocial.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TokenReusedExceptionTest {

    @Test
    void testConstructor() {
        TokenReusedException exception = new TokenReusedException();
        
        assertNotNull(exception);
        assertEquals("Refresh token reuse detected — all sessions invalidated", exception.getMessage());
        assertTrue(exception instanceof AuthException);
    }

    @Test
    void testMessageIsConstant() {
        TokenReusedException exception1 = new TokenReusedException();
        TokenReusedException exception2 = new TokenReusedException();
        
        assertEquals(exception1.getMessage(), exception2.getMessage());
    }
}
