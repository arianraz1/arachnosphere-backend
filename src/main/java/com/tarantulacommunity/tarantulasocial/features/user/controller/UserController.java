package com.tarantulacommunity.tarantulasocial.features.user.controller;

import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.service.UserService;
import com.tarantulacommunity.tarantulasocial.security.config.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---------- PUBLIC ----------

    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    // ---------- PROTECTED ----------

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

    private void assertSelf(Long userId, CustomUserDetails userDetails) {
        if (userDetails == null || !userId.equals(userDetails.getId())) {
            throw new AccessDeniedException("You can only modify your own profile");
        }
    }
}