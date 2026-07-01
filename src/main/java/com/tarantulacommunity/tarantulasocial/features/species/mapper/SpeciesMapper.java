package com.tarantulacommunity.tarantulasocial.features.species.mapper;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesDetailResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SpeciesMapper {

    SpeciesResponse toResponse(SpeciesEntity species);

    SpeciesSummaryResponse toSummaryResponse(SpeciesEntity species);

    SpeciesDetailResponse toDetailResponse(SpeciesResponse species, List<CollectionEntryOwnerResponse> owners);
}