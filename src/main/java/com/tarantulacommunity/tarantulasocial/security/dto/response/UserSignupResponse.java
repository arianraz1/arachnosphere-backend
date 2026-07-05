package com.tarantulacommunity.tarantulasocial.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupResponse {

    private Long id;
    private String email;
    private String username;
    private LocalDateTime createdAt;
    private boolean emailVerified;
}
