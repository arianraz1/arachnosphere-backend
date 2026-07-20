package com.tarantulacommunity.tarantulasocial.features.user.mapper;

import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.request.UpdateUserProfileRequest;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.MeResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserProfileResponse;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.s3.S3StorageService;
import com.tarantulacommunity.tarantulasocial.security.dto.response.UserSignupResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    protected S3StorageService s3StorageService;

    @org.springframework.beans.factory.annotation.Autowired
    public void setS3StorageService(S3StorageService s3StorageService) {
        this.s3StorageService = s3StorageService;
    }

    @Mapping(target = "profilePicUrl", expression =
            "java(user.getProfilePicKey() == null ? null : s3StorageService.getImageUrl(user.getProfilePicKey()))")
    @Mapping(target = "favoriteImageUrl", expression =
            "java(user.getFavoriteImageKey() == null ? null : s3StorageService.getImageUrl(user.getFavoriteImageKey()))")
    public abstract UserProfileResponse toProfileResponse(UserEntity user);

    @Mapping(target = "profilePicUrl", expression =
            "java(user.getProfilePicKey() == null ? null : s3StorageService.getImageUrl(user.getProfilePicKey()))")
    public abstract UserSummaryResponse toSummaryResponse(UserEntity user);

    public abstract UserSignupResponse toSignupResponse(UserEntity user);

    @Mapping(target = "profilePicUrl", expression =
            "java(user.getProfilePicKey() == null ? null : s3StorageService.getImageUrl(user.getProfilePicKey()))")
    @Mapping(target = "favoriteImageUrl", expression =
            "java(user.getFavoriteImageKey() == null ? null : s3StorageService.getImageUrl(user.getFavoriteImageKey()))")
    public abstract MeResponse toMeResponse(UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profilePicKey", ignore = true)
    @Mapping(target = "favoriteImageKey", ignore = true)
    @Mapping(target = "favoriteCaption", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateProfileFromRequest(UpdateUserProfileRequest request, @MappingTarget UserEntity user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "profilePicKey", ignore = true)
    @Mapping(target = "favoriteImageKey", ignore = true)
    @Mapping(target = "bio", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    public abstract void updateFavoriteFromRequest(UpdateUserFavoriteTarantulaRequest request, @MappingTarget UserEntity user);
}