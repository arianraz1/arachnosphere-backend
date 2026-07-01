package com.tarantulacommunity.tarantulasocial.features.species.dto.response;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SpeciesDetailResponse {
    SpeciesResponse species;
    List<CollectionEntryOwnerResponse> owners;
}