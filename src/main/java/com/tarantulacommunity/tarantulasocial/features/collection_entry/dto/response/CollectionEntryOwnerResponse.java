package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response;

import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CollectionEntryOwnerResponse {

    private Long id;
    private UserSummaryResponse user;
    private int slingMale;
    private int slingFemale;
    private int slingUnsexed;
    private int juvenileMale;
    private int juvenileFemale;
    private int juvenileUnsexed;
    private int subadultMale;
    private int subadultFemale;
    private int subadultUnsexed;
    private int adultMale;
    private int adultFemale;
    private int adultUnsexed;
    private String featuredImageUrl;
    private String customInfo;
}
