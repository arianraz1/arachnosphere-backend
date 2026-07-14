package com.tarantulacommunity.tarantulasocial.features.user.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import com.tarantulacommunity.tarantulasocial.security.dto.response.UserSignupResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserMapperTest {

    private S3StorageService s3StorageService;
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        s3StorageService = mock(S3StorageService.class);
        userMapper = new UserMapperImpl();
        userMapper.setS3StorageService(s3StorageService);
    }

    @Test
    void testToProfileResponse() {
        UserEntity user = TestUtilities.verifiedUser("1", "1");
        user.setId(1L);
        user.setProfilePicKey("profile-pictures/pic.jpg");
        user.setBio("I love tarantulas!");
        user.setFavoriteImageKey("favorite-images/favorite.jpg");
        user.setFavoriteCaption("My Brazilian Black");
        user.setCreatedAt(LocalDateTime.now());

        when(s3StorageService.getImageUrl("profile-pictures/pic.jpg"))
                .thenReturn("https://cdn.example.com/profile-pictures/pic.jpg");
        when(s3StorageService.getImageUrl("favorite-images/favorite.jpg"))
                .thenReturn("https://cdn.example.com/favorite-images/favorite.jpg");

        UserProfileResponse response = userMapper.toProfileResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals("https://cdn.example.com/profile-pictures/pic.jpg", response.getProfilePicUrl());
        assertEquals(user.getBio(), response.getBio());
        assertEquals("https://cdn.example.com/favorite-images/favorite.jpg", response.getFavoriteImageUrl());
        assertEquals(user.getFavoriteCaption(), response.getFavoriteCaption());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void testToProfileResponseWithNullKeysSkipsS3Calls() {
        UserEntity user = TestUtilities.verifiedUser("2", "2");
        user.setId(2L);
        user.setCreatedAt(LocalDateTime.now());
        // profilePicKey / favoriteImageKey left null

        UserProfileResponse response = userMapper.toProfileResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertNull(response.getProfilePicUrl());
        assertNull(response.getBio());
        assertNull(response.getFavoriteImageUrl());
        assertNull(response.getFavoriteCaption());
        verifyNoInteractions(s3StorageService);
    }

    @Test
    void testToSummaryResponse() {
        UserEntity user = TestUtilities.verifiedUser("3", "3");
        user.setId(3L);
        user.setProfilePicKey("profile-pictures/pic.jpg");

        when(s3StorageService.getImageUrl("profile-pictures/pic.jpg"))
                .thenReturn("https://cdn.example.com/profile-pictures/pic.jpg");

        UserSummaryResponse response = userMapper.toSummaryResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals("https://cdn.example.com/profile-pictures/pic.jpg", response.getProfilePicUrl());
    }

    @Test
    void testToSummaryResponseWithNullProfilePicKeySkipsS3Call() {
        UserEntity user = TestUtilities.verifiedUser("4", "4");
        user.setId(4L);
        // profilePicKey is null

        UserSummaryResponse response = userMapper.toSummaryResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertNull(response.getProfilePicUrl());
        verifyNoInteractions(s3StorageService);
    }

    @Test
    void testToSignupResponse() {
        UserEntity user = TestUtilities.verifiedUser("5", "5");
        user.setId(5L);
        user.setCreatedAt(LocalDateTime.now());

        UserSignupResponse response = userMapper.toSignupResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void testUpdateProfileFromRequest() {
        UserEntity user = TestUtilities.verifiedUser("6", "6");
        user.setId(6L);
        user.setVersion(0L);

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio("Updated bio!");

        userMapper.updateProfileFromRequest(request, user);

        assertEquals("Updated bio!", user.getBio());
        assertEquals(6L, user.getId());
        assertEquals(TestUtilities.verifiedUser("6", "6").getEmail(), user.getEmail());
    }

    @Test
    void testUpdateProfileFromRequestIgnoresImmutableFields() {
        UserEntity user = TestUtilities.verifiedUser("7", "7");
        user.setId(7L);
        user.setProfilePicKey("profile-pictures/original.jpg");
        Long originalId = user.getId();
        String originalEmail = user.getEmail();
        String originalUsername = user.getUsername();
        String originalPassword = user.getPassword();
        String originalProfilePicKey = user.getProfilePicKey();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio("New bio");

        userMapper.updateProfileFromRequest(request, user);

        assertEquals(originalId, user.getId());
        assertEquals(originalEmail, user.getEmail());
        assertEquals(originalUsername, user.getUsername());
        assertEquals(originalPassword, user.getPassword());
        assertEquals(originalProfilePicKey, user.getProfilePicKey());
    }

    @Test
    void testUpdateProfileFromRequestWithNullValue() {
        UserEntity user = TestUtilities.verifiedUser("8", "8");
        user.setId(8L);
        user.setBio("Old bio");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setBio(null);

        userMapper.updateProfileFromRequest(request, user);

        assertNull(user.getBio());
    }

    @Test
    void testUpdateFavoriteFromRequest() {
        UserEntity user = TestUtilities.verifiedUser("9", "9");
        user.setId(9L);
        user.setVersion(0L);

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption("My GBB");

        userMapper.updateFavoriteFromRequest(request, user);

        assertEquals("My GBB", user.getFavoriteCaption());
    }

    @Test
    void testUpdateFavoriteFromRequestIgnoresImmutableFields() {
        UserEntity user = TestUtilities.verifiedUser("10", "10");
        user.setId(10L);
        user.setProfilePicKey("profile-pictures/pic.jpg");
        user.setFavoriteImageKey("favorite-images/original.jpg");
        String originalBio = "My bio";
        user.setBio(originalBio);

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption("Sling GBB");

        userMapper.updateFavoriteFromRequest(request, user);

        assertEquals("profile-pictures/pic.jpg", user.getProfilePicKey());
        assertEquals("favorite-images/original.jpg", user.getFavoriteImageKey());
        assertEquals(originalBio, user.getBio());
    }

    @Test
    void testUpdateFavoriteFromRequestWithNullValue() {
        UserEntity user = TestUtilities.verifiedUser("11", "11");
        user.setId(11L);
        user.setFavoriteCaption("Old caption");

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption(null);

        userMapper.updateFavoriteFromRequest(request, user);

        assertNull(user.getFavoriteCaption());
    }
}