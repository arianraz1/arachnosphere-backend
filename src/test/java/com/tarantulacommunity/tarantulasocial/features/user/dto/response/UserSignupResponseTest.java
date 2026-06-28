package com.tarantulacommunity.tarantulasocial.features.user.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserSignupResponseTest {

    @Test
    void testNoArgsConstructor() {
        UserSignupResponse response = new UserSignupResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getUsername());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        UserSignupResponse response = new UserSignupResponse(1L, "user@example.com", "tarantula_fan", createdAt);
        
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        LocalDateTime createdAt = LocalDateTime.now();
        UserSignupResponse response = new UserSignupResponse();
        
        response.setId(1L);
        response.setEmail("user@example.com");
        response.setUsername("tarantula_fan");
        response.setCreatedAt(createdAt);
        
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testCanSetNullValues() {
        UserSignupResponse response = new UserSignupResponse();
        
        response.setId(null);
        response.setEmail(null);
        response.setUsername(null);
        response.setCreatedAt(null);
        
        assertNull(response.getId());
        assertNull(response.getEmail());
        assertNull(response.getUsername());
        assertNull(response.getCreatedAt());
    }
}
