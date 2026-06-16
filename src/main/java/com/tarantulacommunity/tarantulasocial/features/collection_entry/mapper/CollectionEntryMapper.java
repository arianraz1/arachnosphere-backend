package com.tarantulacommunity.tarantulasocial.features.collection_entry.mapper;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SpeciesMapper.class, UserMapper.class})
public interface CollectionEntryMapper {

    CollectionEntryResponse toResponse(CollectionEntryEntity entry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "species", ignore = true)
    @Mapping(target = "version", ignore = true)
    CollectionEntryEntity toEntity(CreateCollectionEntryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "species", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntityFromRequest(UpdateCollectionEntryRequest request, @MappingTarget CollectionEntryEntity entry);
}
