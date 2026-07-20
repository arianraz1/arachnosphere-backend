package com.tarantulacommunity.tarantulasocial.features.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeResponse {

    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private String profilePicUrl;
    private String bio;
    private String favoriteImageUrl;
    private String favoriteCaption;
    private LocalDateTime createdAt;
}