package com.tarantulacommunity.tarantulasocial.features.user.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileResponseTest {

    @Test
    void testNoArgsConstructor() {
        UserProfileResponse response = new UserProfileResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUsername());
        assertNull(response.getProfilePicUrl());
        assertNull(response.getBio());
        assertNull(response.getFavoriteImageUrl());
        assertNull(response.getFavoriteCaption());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now();
        UserProfileResponse response = new UserProfileResponse(
            1L, "tarantula_fan", "http://example.com/pic.jpg",
            "Tarantula enthusiast", "http://example.com/fav.jpg",
            "My favorite species", createdAt
        );
        
        assertEquals(1L, response.getId());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals("http://example.com/pic.jpg", response.getProfilePicUrl());
        assertEquals("Tarantula enthusiast", response.getBio());
        assertEquals("http://example.com/fav.jpg", response.getFavoriteImageUrl());
        assertEquals("My favorite species", response.getFavoriteCaption());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        UserProfileResponse response = new UserProfileResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setId(1L);
        response.setUsername("tarantula_fan");
        response.setProfilePicUrl("http://example.com/pic.jpg");
        response.setBio("Tarantula enthusiast");
        response.setFavoriteImageUrl("http://example.com/fav.jpg");
        response.setFavoriteCaption("My favorite species");
        response.setCreatedAt(createdAt);
        
        assertEquals(1L, response.getId());
        assertEquals("tarantula_fan", response.getUsername());
        assertEquals("http://example.com/pic.jpg", response.getProfilePicUrl());
        assertEquals("Tarantula enthusiast", response.getBio());
        assertEquals("http://example.com/fav.jpg", response.getFavoriteImageUrl());
        assertEquals("My favorite species", response.getFavoriteCaption());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
