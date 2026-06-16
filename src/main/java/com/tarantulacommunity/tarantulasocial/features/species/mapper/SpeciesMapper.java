package com.tarantulacommunity.tarantulasocial.features.species.mapper;

import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpeciesMapper {

    SpeciesResponse toResponse(SpeciesEntity species);

    SpeciesSummaryResponse toSummaryResponse(SpeciesEntity species);
}