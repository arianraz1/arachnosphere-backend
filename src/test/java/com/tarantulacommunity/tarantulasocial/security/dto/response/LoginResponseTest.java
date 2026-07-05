package com.tarantulacommunity.tarantulasocial.security.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginResponseTest {

    @Test
    void testNoArgsConstructor() {
        LoginResponse response = new LoginResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getUsername());
        assertFalse(response.isEmailVerified());
    }

    @Test
    void testAllArgsConstructor() {
        LoginResponse response = new LoginResponse(1L, "user@example.com", "tarantula_fan", true);

        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("tarantula_fan", response.getUsername());
        assertTrue(response.isEmailVerified());
    }

    @Test
    void testSettersAndGetters() {
        LoginResponse response = new LoginResponse();

        response.setId(1L);
        response.setEmail("user@example.com");
        response.setUsername("tarantula_fan");
        response.setEmailVerified(true);

        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("tarantula_fan", response.getUsername());
        assertTrue(response.isEmailVerified());
    }

    @Test
    void testEmailVerifiedDefaultsFalse() {
        LoginResponse response = new LoginResponse();
        assertFalse(response.isEmailVerified());
    }

    @Test
    void testEmailVerifiedCanBeSetToFalse() {
        LoginResponse response = new LoginResponse(1L, "user@example.com", "tarantula_fan", false);
        assertFalse(response.isEmailVerified());
    }
}
