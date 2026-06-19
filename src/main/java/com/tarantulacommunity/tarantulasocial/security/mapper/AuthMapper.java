package com.tarantulacommunity.tarantulasocial.security.mapper;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginRequest;
import com.tarantulacommunity.tarantulasocial.security.dto.LoginResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    LoginResponse toLoginResponse(UserEntity user);

    default LoginRequest toLoginRequest(LoginRequest request) {
        return request;
    }
}
