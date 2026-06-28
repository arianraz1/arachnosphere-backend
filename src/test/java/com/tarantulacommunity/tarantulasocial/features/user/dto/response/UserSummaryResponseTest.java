package com.tarantulacommunity.tarantulasocial.features.user.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserSummaryResponseTest {

    @Test
    void testNoArgsConstructor() {
        UserSummaryResponse response = new UserSummaryResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUsername());
        assertNull(response.getProfilePicUrl());
    }

    @Test
    void testAllArgsConstructor() {
        UserSummaryResponse response = new UserSummaryResponse(
            1L, "tarantula_fan", "http://example.com/profile.jpg"
        );
        
        assertEquals(1L, response.getId());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals("http://example.com/profile.jpg", response.getProfilePicUrl());
    }

    @Test
    void testSettersAndGetters() {
        UserSummaryResponse response = new UserSummaryResponse();
        
        response.setId(1L);
        response.setUsername("tarantula_fan");
        response.setProfilePicUrl("http://example.com/profile.jpg");
        
        assertEquals(1L, response.getId());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals("http://example.com/profile.jpg", response.getProfilePicUrl());
    }

    @Test
    void testCanSetNullValues() {
        UserSummaryResponse response = new UserSummaryResponse();
        
        response.setId(null);
        response.setUsername(null);
        response.setProfilePicUrl(null);
        
        assertNull(response.getId());
        assertNull(response.getUsername());
        assertNull(response.getProfilePicUrl());
    }
}
