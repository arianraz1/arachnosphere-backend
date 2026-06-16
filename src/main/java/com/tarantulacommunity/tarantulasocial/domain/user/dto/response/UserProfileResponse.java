package com.tarantulacommunity.tarantulasocial.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long id;
    private String username;
    private String profilePicUrl;
    private String bio;
    private String favoriteImageUrl;
    private String favoriteCaption;

    private LocalDateTime createdAt;
}