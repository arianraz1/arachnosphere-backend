package com.tarantulacommunity.tarantulasocial.features.user.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MeResponseTest {

    @Test
    void settersAndGetters_workCorrectly() {
        LocalDateTime now = LocalDateTime.now();

        MeResponse response = new MeResponse();
        response.setId(1L);
        response.setUsername("testuser");
        response.setEmail("test@example.com");
        response.setEmailVerified(true);
        response.setProfilePicUrl("https://cdn.example.com/profile-pictures/pic.jpg");
        response.setBio("I love tarantulas!");
        response.setFavoriteImageUrl("https://cdn.example.com/favorite-images/favorite.jpg");
        response.setFavoriteCaption("My Brazilian Black");
        response.setCreatedAt(now);

        assertEquals(1L, response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertTrue(response.isEmailVerified());
        assertEquals("https://cdn.example.com/profile-pictures/pic.jpg", response.getProfilePicUrl());
        assertEquals("I love tarantulas!", response.getBio());
        assertEquals("https://cdn.example.com/favorite-images/favorite.jpg", response.getFavoriteImageUrl());
        assertEquals("My Brazilian Black", response.getFavoriteCaption());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void noArgsConstructor_leavesFieldsNullOrDefault() {
        MeResponse response = new MeResponse();

        assertNull(response.getId());
        assertNull(response.getUsername());
        assertNull(response.getEmail());
        assertFalse(response.isEmailVerified());
        assertNull(response.getProfilePicUrl());
        assertNull(response.getBio());
        assertNull(response.getFavoriteImageUrl());
        assertNull(response.getFavoriteCaption());
        assertNull(response.getCreatedAt());
    }

    @Test
    void allArgsConstructor_setsFieldsInDeclaredOrder() {
        LocalDateTime now = LocalDateTime.now();

        MeResponse response = new MeResponse(
                2L, "user2", "user2@example.com", true,
                null, null, null, null, now
        );

        assertEquals(2L, response.getId());
        assertEquals("user2", response.getUsername());
        assertEquals("user2@example.com", response.getEmail());
        assertTrue(response.isEmailVerified());
        assertEquals(now, response.getCreatedAt());
    }
}