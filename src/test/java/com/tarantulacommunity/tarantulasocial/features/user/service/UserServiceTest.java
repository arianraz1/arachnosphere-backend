package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.MeResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void register_savesUserAndReturnsEntity() {
        UserRegisterRequest request = new UserRegisterRequest("TEST@Example.COM", "TestUser", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("$2a$10$hashedpassword");

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setEmail("test@example.com");
        savedUser.setUsername("testuser");
        savedUser.setPassword("$2a$10$hashedpassword");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserEntity result = userService.registerUser(request);

        assertEquals(savedUser, result);
        verify(userRepository).save(any(UserEntity.class));
        verify(passwordEncoder).encode("password123");
        verifyNoInteractions(userMapper);
    }

    @Test
    void register_normalizesEmailToLowercase() {
        UserRegisterRequest request = new UserRegisterRequest("TEST@EXAMPLE.COM", "testuser", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.registerUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void register_normalizesUsernameToLowercase() {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "TestUser", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashed");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        userService.registerUser(request);

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("testuser");
    }

    @Test
    void register_throwsEmailAlreadyTakenException_whenEmailExists() {
        UserRegisterRequest request = new UserRegisterRequest("TEST@Example.COM", "testuser", "password123");

        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(GlobalExceptionHandler.EmailAlreadyTakenException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode("password123");
    }

    @Test
    void register_throwsUsernameAlreadyTakenException_whenUsernameExists() {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "TestUser", "password123");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(GlobalExceptionHandler.UsernameAlreadyTakenException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode("password123");
    }

    @Test
    void getProfile_returnsUserProfileResponse() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setProfilePicKey("profile-pictures/abc.jpg");
        user.setBio("This is a bio");
        user.setFavoriteImageKey("favorite-images/xyz.jpg");
        user.setFavoriteCaption("This is a caption");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse expectedResponse = new UserProfileResponse(
                1L, "testuser", "https://cdn.example.com/profile-pictures/abc.jpg",
                "This is a bio", "https://cdn.example.com/favorite-images/xyz.jpg",
                "This is a caption", null);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.getProfile(1L);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(1L);
        verify(userMapper).toProfileResponse(user);
    }

    @Test
    void getProfile_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () -> userService.getProfile(99L));
        verify(userRepository).findById(99L);
        verify(userMapper, never()).toProfileResponse(any());
    }

    @Test
    void updateProfile_updatesBioViaModifyingQueryAndReturnsFreshResponse() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("My bio");

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setBio("Old bio");

        UserEntity refreshedUser = new UserEntity();
        refreshedUser.setId(1L);
        refreshedUser.setBio("My bio");

        // first findById is the existence check, second is the post-update re-fetch
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser), Optional.of(refreshedUser));

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser",
                null, "My bio", null, null, null);
        when(userMapper.toProfileResponse(refreshedUser)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertEquals(expectedResponse, result);
        verify(userRepository).updateBio(eq(1L), eq("My bio"), any(LocalDateTime.class));
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateProfileFromRequest(any(), any());
    }

    @Test
    void updateProfile_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                userService.updateProfile(99L, new UpdateUserProfileRequest(null)));

        verify(userRepository, never()).updateBio(any(), any(), any());
    }

    @Test
    void updateFavorite_updatesCaptionViaModifyingQueryAndReturnsFreshResponse() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest("My favorite GBB");

        UserEntity existingUser = new UserEntity();
        existingUser.setId(1L);
        existingUser.setFavoriteCaption("Old caption");

        UserEntity refreshedUser = new UserEntity();
        refreshedUser.setId(1L);
        refreshedUser.setFavoriteCaption("My favorite GBB");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser), Optional.of(refreshedUser));

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser", null, null,
                null, "My favorite GBB", null);
        when(userMapper.toProfileResponse(refreshedUser)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.updateFavorite(1L, request);

        assertEquals(expectedResponse, result);
        verify(userRepository).updateFavoriteCaption(eq(1L), eq("My favorite GBB"), any(LocalDateTime.class));
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).updateFavoriteFromRequest(any(), any());
    }

    @Test
    void updateFavorite_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                userService.updateFavorite(99L, new UpdateUserFavoriteTarantulaRequest(null)));

        verify(userRepository, never()).updateFavoriteCaption(any(), any(), any());
    }

    @Test
    void getMe_returnsMeResponse() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setEmailVerified(true);
        user.setProfilePicKey("profile-pictures/abc.jpg");
        user.setBio("This is a bio");
        user.setFavoriteImageKey("favorite-images/xyz.jpg");
        user.setFavoriteCaption("This is a caption");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        MeResponse expectedResponse = new MeResponse();
        expectedResponse.setId(1L);
        expectedResponse.setUsername("testuser");
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setEmailVerified(true);
        expectedResponse.setProfilePicUrl("https://cdn.example.com/profile-pictures/abc.jpg");
        expectedResponse.setBio("This is a bio");
        expectedResponse.setFavoriteImageUrl("https://cdn.example.com/favorite-images/xyz.jpg");
        expectedResponse.setFavoriteCaption("This is a caption");

        when(userMapper.toMeResponse(user)).thenReturn(expectedResponse);

        MeResponse result = userService.getMe(1L);

        assertEquals(expectedResponse, result);
        verify(userRepository).findById(1L);
        verify(userMapper).toMeResponse(user);
    }

    @Test
    void getMe_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () -> userService.getMe(99L));
        verify(userRepository).findById(99L);
        verify(userMapper, never()).toMeResponse(any());
    }
}