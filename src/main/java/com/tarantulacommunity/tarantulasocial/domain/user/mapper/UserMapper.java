package com.tarantulacommunity.tarantulasocial.domain.user.mapper;

import com.tarantulacommunity.tarantulasocial.domain.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.domain.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.domain.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.domain.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.domain.user.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserProfileResponse toProfileResponse(UserEntity user);

    UserSummaryResponse toSummaryResponse(UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "favoriteImageUrl", ignore = true)
    @Mapping(target = "favoriteCaption", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateProfileFromRequest(UpdateUserProfileRequest request, @MappingTarget UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profilePicUrl", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateFavoriteFromRequest(UpdateUserFavoriteTarantulaRequest request, @MappingTarget UserEntity user);
}