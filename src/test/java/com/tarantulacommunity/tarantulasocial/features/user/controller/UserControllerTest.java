package com.tarantulacommunity.tarantulasocial.features.user.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserImageService;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserImageService userImageService;

    @InjectMocks
    private UserController userController;

    private CustomUserDetails userDetailsOf(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return new CustomUserDetails(user, null);
    }

    private MultipartFile fakeFile() {
        return new MockMultipartFile("file", "tarantula.jpg", "image/jpeg", "fake-image-bytes".getBytes());
    }

    @Test
    void getProfile_shouldReturnUserProfileResponse() {
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        when(userService.getProfile(1L)).thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response = userController.getProfile(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void getProfile_shouldThrowWhenUserNotFound() {
        when(userService.getProfile(99L))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThatThrownBy(() -> userController.getProfile(99L))
                .isInstanceOf(GlobalExceptionHandler.UserNotFoundException.class);
    }

    @Test
    void updateProfile_shouldReturnUpdatedProfileWhenUserIsSelf() {
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null);

        when(userService.updateProfile(eq(1L), any(UpdateUserProfileRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response =
                userController.updateProfile(1L, userDetailsOf(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void updateProfile_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null);

        assertThatThrownBy(() -> userController.updateProfile(1L, userDetailsOf(2L), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void updateProfile_shouldThrowWhenUserNotFound() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null);

        when(userService.updateProfile(eq(99L), any(UpdateUserProfileRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThatThrownBy(() -> userController.updateProfile(99L, userDetailsOf(99L), request))
                .isInstanceOf(GlobalExceptionHandler.UserNotFoundException.class);
    }

    @Test
    void updateProfile_shouldThrowAccessDeniedWhenUserDetailsIsNull() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null);

        assertThatThrownBy(() -> userController.updateProfile(1L, null, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void updateFavorite_shouldReturnUpdatedProfileWhenUserIsSelf() {
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null);

        when(userService.updateFavorite(eq(1L), any(UpdateUserFavoriteTarantulaRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response =
                userController.updateFavorite(1L, userDetailsOf(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void updateFavorite_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null);

        assertThatThrownBy(() -> userController.updateFavorite(1L, userDetailsOf(2L), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void updateFavorite_shouldThrowWhenUserNotFound() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null);

        when(userService.updateFavorite(eq(99L), any(UpdateUserFavoriteTarantulaRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThatThrownBy(() -> userController.updateFavorite(99L, userDetailsOf(99L), request))
                .isInstanceOf(GlobalExceptionHandler.UserNotFoundException.class);
    }

    @Test
    void updateFavorite_shouldThrowAccessDeniedWhenUserDetailsIsNull() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null);

        assertThatThrownBy(() -> userController.updateFavorite(1L, null, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void uploadProfilePicture_shouldReturnUpdatedProfileWhenUserIsSelf() {
        MultipartFile file = fakeFile();
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        when(userService.getProfile(1L)).thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response =
                userController.uploadProfilePicture(1L, file, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(userImageService).uploadProfilePicture(1L, file);
    }

    @Test
    void uploadProfilePicture_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        MultipartFile file = fakeFile();

        assertThatThrownBy(() -> userController.uploadProfilePicture(1L, file, userDetailsOf(2L)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
        verifyNoInteractions(userImageService);
    }

    @Test
    void uploadProfilePicture_shouldThrowAccessDeniedWhenUserDetailsIsNull() {
        MultipartFile file = fakeFile();

        assertThatThrownBy(() -> userController.uploadProfilePicture(1L, file, null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(userImageService);
    }

    @Test
    void uploadProfilePicture_shouldPropagateImageUploadException() {
        MultipartFile file = fakeFile();
        doThrow(new GlobalExceptionHandler.ImageUploadException("Failed to read uploaded file", new java.io.IOException()))
                .when(userImageService).uploadProfilePicture(1L, file);

        assertThatThrownBy(() -> userController.uploadProfilePicture(1L, file, userDetailsOf(1L)))
                .isInstanceOf(GlobalExceptionHandler.ImageUploadException.class);
    }

    @Test
    void uploadFavoriteImage_shouldReturnUpdatedProfileWhenUserIsSelf() {
        MultipartFile file = fakeFile();
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        when(userService.getProfile(1L)).thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response =
                userController.uploadFavoriteImage(1L, file, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(userImageService).uploadFavoriteImage(1L, file);
    }

    @Test
    void uploadFavoriteImage_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        MultipartFile file = fakeFile();

        assertThatThrownBy(() -> userController.uploadFavoriteImage(1L, file, userDetailsOf(2L)))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
        verifyNoInteractions(userImageService);
    }

    @Test
    void uploadFavoriteImage_shouldThrowAccessDeniedWhenUserDetailsIsNull() {
        MultipartFile file = fakeFile();

        assertThatThrownBy(() -> userController.uploadFavoriteImage(1L, file, null))
                .isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(userImageService);
    }

    @Test
    void uploadFavoriteImage_shouldPropagateImageUploadException() {
        MultipartFile file = fakeFile();
        doThrow(new GlobalExceptionHandler.ImageUploadException("Failed to read uploaded file", new java.io.IOException()))
                .when(userImageService).uploadFavoriteImage(1L, file);

        assertThatThrownBy(() -> userController.uploadFavoriteImage(1L, file, userDetailsOf(1L)))
                .isInstanceOf(GlobalExceptionHandler.ImageUploadException.class);
    }
}