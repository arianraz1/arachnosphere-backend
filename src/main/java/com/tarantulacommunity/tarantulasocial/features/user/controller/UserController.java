package com.tarantulacommunity.tarantulasocial.features.user.controller;

import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserImageService;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserImageService userImageService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PutMapping("/{userId}/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserProfileRequest request) {
        assertSelf(userId, userDetails);
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }

    @PutMapping("/{userId}/favorite")
    public ResponseEntity<UserProfileResponse> updateFavorite(
            @PathVariable Long userId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateUserFavoriteTarantulaRequest request) {
        assertSelf(userId, userDetails);
        return ResponseEntity.ok(userService.updateFavorite(userId, request));
    }

    @PostMapping("/{userId}/profile/image")
    public ResponseEntity<UserProfileResponse> uploadProfilePicture(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        assertSelf(userId, userDetails);
        userImageService.uploadProfilePicture(userId, file);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    @PostMapping("/{userId}/favorite/image")
    public ResponseEntity<UserProfileResponse> uploadFavoriteImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        assertSelf(userId, userDetails);
        userImageService.uploadFavoriteImage(userId, file);
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    private void assertSelf(Long userId, CustomUserDetails userDetails) {
        if (userDetails == null || !userId.equals(userDetails.getId())) {
            throw new AccessDeniedException("You can only modify your own profile");
        }
    }
}