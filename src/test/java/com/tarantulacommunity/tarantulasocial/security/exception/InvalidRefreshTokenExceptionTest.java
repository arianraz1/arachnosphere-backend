package com.tarantulacommunity.tarantulasocial.security.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidRefreshTokenExceptionTest {

    @Test
    void testConstructorWithMessage() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException("Invalid token");
        
        assertNotNull(exception);
        assertEquals("Invalid token", exception.getMessage());
        assertTrue(exception instanceof AuthException);
    }

    @Test
    void testConstructorWithNullMessage() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException(null);
        
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithEmptyMessage() {
        InvalidRefreshTokenException exception = new InvalidRefreshTokenException("");
        
        assertNotNull(exception);
        assertEquals("", exception.getMessage());
    }
}
