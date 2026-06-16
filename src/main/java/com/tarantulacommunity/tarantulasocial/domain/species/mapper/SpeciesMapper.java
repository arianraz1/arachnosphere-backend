package com.tarantulacommunity.tarantulasocial.domain.species.mapper;

import com.tarantulacommunity.tarantulasocial.domain.species.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.domain.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.domain.species.dto.response.SpeciesSummaryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SpeciesMapper {

    SpeciesResponse toResponse(SpeciesEntity species);

    SpeciesSummaryResponse toSummaryResponse(SpeciesEntity species);
}