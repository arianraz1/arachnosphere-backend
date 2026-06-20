package com.tarantulacommunity.tarantulasocial.features.user.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

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

        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, null);

        when(userService.updateProfile(eq(1L), any(UpdateUserProfileRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response = userController.updateProfile(1L, 1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void updateProfile_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, null);

        assertThatThrownBy(() -> userController.updateProfile(1L, 2L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void updateProfile_shouldThrowWhenUserNotFound() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest(null, null);

        when(userService.updateProfile(eq(99L), any(UpdateUserProfileRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThatThrownBy(() -> userController.updateProfile(99L, 99L, request))
                .isInstanceOf(GlobalExceptionHandler.UserNotFoundException.class);
    }

    @Test
    void updateFavorite_shouldReturnUpdatedProfileWhenUserIsSelf() {
        UserProfileResponse expectedResponse = new UserProfileResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");

        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null, null);

        when(userService.updateFavorite(eq(1L), any(UpdateUserFavoriteTarantulaRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<UserProfileResponse> response = userController.updateFavorite(1L, 1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void updateFavorite_shouldThrowAccessDeniedWhenUserIsNotSelf() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null, null);

        assertThatThrownBy(() -> userController.updateFavorite(1L, 2L, request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You can only modify your own profile");
    }

    @Test
    void updateFavorite_shouldThrowWhenUserNotFound() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(null, null);

        when(userService.updateFavorite(eq(99L), any(UpdateUserFavoriteTarantulaRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThatThrownBy(() -> userController.updateFavorite(99L, 99L, request))
                .isInstanceOf(GlobalExceptionHandler.UserNotFoundException.class);
    }
}
