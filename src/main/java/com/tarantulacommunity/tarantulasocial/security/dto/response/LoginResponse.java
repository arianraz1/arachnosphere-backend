package com.tarantulacommunity.tarantulasocial.security.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private Long id;
    private String email;
    private String username;
    private boolean emailVerified;
}