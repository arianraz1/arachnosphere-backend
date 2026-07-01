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
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new GlobalExceptionHandler.EmailAlreadyTakenException(request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new GlobalExceptionHandler.UsernameAlreadyTakenException(request.getUsername());
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
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