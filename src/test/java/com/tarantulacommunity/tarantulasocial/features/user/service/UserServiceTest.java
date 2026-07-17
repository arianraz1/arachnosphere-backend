package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.security.dto.response.UserSignupResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    void updateProfile_updatesAndReturnsResponse() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("My bio");

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser",
                null, "My bio", null, null, null);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.updateProfile(1L, request);

        assertEquals(expectedResponse, result);
        verify(userMapper).updateProfileFromRequest(request, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateProfile_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                userService.updateProfile(99L, new UpdateUserProfileRequest(null)));
    }

    @Test
    void updateFavorite_updatesAndReturnsResponse() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest("My favorite GBB");

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser", null, null,
                null, "My favorite GBB", null);
        when(userMapper.toProfileResponse(user)).thenReturn(expectedResponse);

        UserProfileResponse result = userService.updateFavorite(1L, request);

        assertEquals(expectedResponse, result);
        verify(userMapper).updateFavoriteFromRequest(request, user);
        verify(userRepository).save(user);
    }

    @Test
    void updateFavorite_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                userService.updateFavorite(99L, new UpdateUserFavoriteTarantulaRequest(null)));
    }
}