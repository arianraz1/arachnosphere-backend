package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

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
    void register_savesUserAndReturnsSignupResponse() {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "testuser", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("$2a$10$hashedpassword");

        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setEmail(request.getEmail());
        savedUser.setUsername(request.getUsername());
        savedUser.setPassword("$2a$10$hashedpassword");

        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserSignupResponse expectedResponse = new UserSignupResponse(1L, "test@example.com", "testuser", null);
        when(userMapper.toSignupResponse(savedUser)).thenReturn(expectedResponse);

        UserSignupResponse result = userService.registerUser(request);

        assertEquals(expectedResponse, result);
        verify(userRepository).save(any(UserEntity.class));
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_throwsEmailAlreadyTakenException_whenEmailExists() {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "testuser", "password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(GlobalExceptionHandler.EmailAlreadyTakenException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode("password123");
    }

    @Test
    void register_throwsUsernameAlreadyTakenException_whenUsernameExists() {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "testuser", "password123");

        when(userRepository.existsByUsername(request.getUsername())).thenReturn(true);

        assertThrows(GlobalExceptionHandler.UsernameAlreadyTakenException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
        verify(passwordEncoder, never()).encode("password123");
    }

    @Test
    void getProfile_returnsUserProfileResponse() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("testuser");
        user.setProfilePicUrl("https://example.com/profile.jpg");
        user.setBio("This is a bio");
        user.setFavoriteCaption("This is a caption");
        user.setFavoriteImageUrl("https://example.com/favorite.jpg");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser", "https://example.com/profile.jpg", "This is a bio", "This is a caption", "https://example.com/favorite.jpg", null);
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
        UpdateUserProfileRequest request = new UpdateUserProfileRequest("https://example.com/pic.jpg", "My bio");

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser",
                "https://example.com/pic.jpg", "My bio", null, null, null);
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
                userService.updateProfile(99L, new UpdateUserProfileRequest(null, null)));
    }

    @Test
    void updateFavorite_updatesAndReturnsResponse() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest(
                "https://example.com/tarantula.jpg", "My favorite GBB");

        UserEntity user = new UserEntity();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UserProfileResponse expectedResponse = new UserProfileResponse(1L, "testuser", null, null,
                "https://example.com/tarantula.jpg", "My favorite GBB", null);
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
                userService.updateFavorite(99L, new UpdateUserFavoriteTarantulaRequest(null, null)));
    }
}
