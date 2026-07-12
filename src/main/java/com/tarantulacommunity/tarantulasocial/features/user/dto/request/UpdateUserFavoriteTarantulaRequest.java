package com.tarantulacommunity.tarantulasocial.features.user.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserFavoriteTarantulaRequest {

    @Size(max = 255)
    private String favoriteCaption;
}
