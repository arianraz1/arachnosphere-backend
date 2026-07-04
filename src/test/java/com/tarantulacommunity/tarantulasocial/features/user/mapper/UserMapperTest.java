package com.tarantulacommunity.tarantulasocial.features.user.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.security.dto.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    void testToProfileResponse() {
        UserEntity user = TestUtilities.verifiedUser("1", "1");
        user.setId(1L);
        user.setProfilePicUrl("https://example.com/pic.jpg");
        user.setBio("I love tarantulas!");
        user.setFavoriteImageUrl("https://example.com/favorite.jpg");
        user.setFavoriteCaption("My Brazilian Black");
        user.setCreatedAt(LocalDateTime.now());

        UserProfileResponse response = userMapper.toProfileResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getProfilePicUrl(), response.getProfilePicUrl());
        assertEquals(user.getBio(), response.getBio());
        assertEquals(user.getFavoriteImageUrl(), response.getFavoriteImageUrl());
        assertEquals(user.getFavoriteCaption(), response.getFavoriteCaption());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void testToProfileResponseWithNullOptionalFields() {
        UserEntity user = TestUtilities.verifiedUser("2", "2");
        user.setId(2L);
        user.setCreatedAt(LocalDateTime.now());

        UserProfileResponse response = userMapper.toProfileResponse(user);

        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertNull(response.getProfilePicUrl());
        assertNull(response.getBio());
        assertNull(response.getFavoriteImageUrl());
        assertNull(response.getFavoriteCaption());
    }

    @Test
    void testToSummaryResponse() {
        // Arrange
        UserEntity user = TestUtilities.verifiedUser("3", "3");
        user.setId(3L);
        user.setProfilePicUrl("https://example.com/pic.jpg");

        // Act
        UserSummaryResponse response = userMapper.toSummaryResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getProfilePicUrl(), response.getProfilePicUrl());
    }

    @Test
    void testToSummaryResponseWithNullProfilePic() {
        // Arrange
        UserEntity user = TestUtilities.verifiedUser("4", "4");
        user.setId(4L);
        // profilePicUrl is null

        // Act
        UserSummaryResponse response = userMapper.toSummaryResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertNull(response.getProfilePicUrl());
    }

    @Test
    void testToSignupResponse() {
        // Arrange
        UserEntity user = TestUtilities.verifiedUser("5", "5");
        user.setId(5L);
        user.setCreatedAt(LocalDateTime.now());

        // Act
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
        request.setProfilePicUrl("https://example.com/new-pic.jpg");
        request.setBio("Updated bio!");

        userMapper.updateProfileFromRequest(request, user);

        assertEquals("https://example.com/new-pic.jpg", user.getProfilePicUrl());
        assertEquals("Updated bio!", user.getBio());
        assertEquals(6L, user.getId());
        assertEquals(TestUtilities.verifiedUser("6", "6").getEmail(), user.getEmail());
    }

    @Test
    void testUpdateProfileFromRequestIgnoresImmutableFields() {
        UserEntity user = TestUtilities.verifiedUser("7", "7");
        user.setId(7L);
        Long originalId = user.getId();
        String originalEmail = user.getEmail();
        String originalUsername = user.getUsername();
        String originalPassword = user.getPassword();

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("https://example.com/pic.jpg");
        request.setBio("New bio");

        userMapper.updateProfileFromRequest(request, user);

        assertEquals(originalId, user.getId());
        assertEquals(originalEmail, user.getEmail());
        assertEquals(originalUsername, user.getUsername());
        assertEquals(originalPassword, user.getPassword());
    }

    @Test
    void testUpdateProfileFromRequestWithNullValues() {
        UserEntity user = TestUtilities.verifiedUser("8", "8");
        user.setId(8L);
        user.setProfilePicUrl("https://example.com/pic.jpg");
        user.setBio("Old bio");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl(null);
        request.setBio(null);

        userMapper.updateProfileFromRequest(request, user);

        assertNull(user.getProfilePicUrl());
        assertNull(user.getBio());
    }

    @Test
    void testUpdateFavoriteFromRequest() {
        UserEntity user = TestUtilities.verifiedUser("9", "9");
        user.setId(9L);
        user.setVersion(0L);

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("https://example.com/favorite.jpg");
        request.setFavoriteCaption("My GBB");

        userMapper.updateFavoriteFromRequest(request, user);

        assertEquals("https://example.com/favorite.jpg", user.getFavoriteImageUrl());
        assertEquals("My GBB", user.getFavoriteCaption());
    }

    @Test
    void testUpdateFavoriteFromRequestIgnoresImmutableFields() {
        UserEntity user = TestUtilities.verifiedUser("10", "10");
        user.setId(10L);
        String originalProfilePicUrl = "https://example.com/pic.jpg";
        String originalBio = "My bio";
        user.setProfilePicUrl(originalProfilePicUrl);
        user.setBio(originalBio);

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("https://example.com/favorite.jpg");
        request.setFavoriteCaption("Sling GBB");

        userMapper.updateFavoriteFromRequest(request, user);

        assertEquals(originalProfilePicUrl, user.getProfilePicUrl());
        assertEquals(originalBio, user.getBio());
    }

    @Test
    void testUpdateFavoriteFromRequestWithNullValues() {
        UserEntity user = TestUtilities.verifiedUser("11", "11");
        user.setId(11L);
        user.setFavoriteImageUrl("https://example.com/favorite.jpg");
        user.setFavoriteCaption("Old caption");

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl(null);
        request.setFavoriteCaption(null);

        userMapper.updateFavoriteFromRequest(request, user);

        assertNull(user.getFavoriteImageUrl());
        assertNull(user.getFavoriteCaption());
    }
}

