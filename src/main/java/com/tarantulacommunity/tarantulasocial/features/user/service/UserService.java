package com.tarantulacommunity.tarantulasocial.features.user.service;

import com.tarantulacommunity.tarantulasocial.exception.EmailAlreadyTakenException;
import com.tarantulacommunity.tarantulasocial.exception.UserNotFoundException;
import com.tarantulacommunity.tarantulasocial.exception.UsernameAlreadyTakenException;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UserRegisterRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSignupResponse;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserSignupResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyTakenException(request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyTakenException(request.getUsername());
        }

        UserEntity user = new UserEntity();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toSignupResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return userMapper.toProfileResponse(findUserOrThrow(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        UserEntity user = findUserOrThrow(userId);
        userMapper.updateProfileFromRequest(request, user);
        return userMapper.toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateFavorite(Long userId, UpdateUserFavoriteTarantulaRequest request) {
        UserEntity user = findUserOrThrow(userId);
        userMapper.updateFavoriteFromRequest(request, user);
        return userMapper.toProfileResponse(user);
    }

    private UserEntity findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}