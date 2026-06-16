package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCollectionEntryRequest {

    @Min(0)
    private int slingMale;
    @Min(0)
    private int slingFemale;
    @Min(0)
    private int slingUnsexed;

    @Min(0)
    private int juvenileMale;
    @Min(0)
    private int juvenileFemale;
    @Min(0)
    private int juvenileUnsexed;

    @Min(0)
    private int subadultMale;
    @Min(0)
    private int subadultFemale;
    @Min(0)
    private int subadultUnsexed;

    @Min(0)
    private int adultMale;
    @Min(0)
    private int adultFemale;
    @Min(0)
    private int adultUnsexed;

    @Size(max = 2048)
    private String featuredImageUrl;

    @Size(max = 1000)
    private String customInfo;
}