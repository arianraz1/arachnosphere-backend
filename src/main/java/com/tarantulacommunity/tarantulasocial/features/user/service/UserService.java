package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.security.dto.response.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponse registerUser(UserRegisterRequest request) {
        String normalizedEmail = request.getEmail().toLowerCase().trim();
        String normalizedUsername = request.getUsername().toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new GlobalExceptionHandler.EmailAlreadyTakenException(normalizedEmail);
        }
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new GlobalExceptionHandler.UsernameAlreadyTakenException(normalizedUsername);
        }

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setUsername(normalizedUsername);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toSignupResponse(userRepository.save(user));
    }

    public UserProfileResponse getProfile(Long userId) {
        return userMapper.toProfileResponse(findUserOrThrow(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        UserEntity user = findUserOrThrow(userId);
        userMapper.updateProfileFromRequest(request, user);
        return userMapper.toProfileResponse(userRepository.save(user));
    }

    @Transactional
    public UserProfileResponse updateFavorite(Long userId, UpdateUserFavoriteTarantulaRequest request) {
        UserEntity user = findUserOrThrow(userId);
        userMapper.updateFavoriteFromRequest(request, user);
        return userMapper.toProfileResponse(userRepository.save(user));
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));
    }
}