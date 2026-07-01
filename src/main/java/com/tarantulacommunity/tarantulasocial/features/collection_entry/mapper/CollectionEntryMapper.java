package com.tarantulacommunity.tarantulasocial.features.collection_entry.mapper;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {SpeciesMapper.class, UserMapper.class})
public interface CollectionEntryMapper {

    CollectionEntryResponse toResponse(CollectionEntryEntity entry);

    CollectionEntryOwnerResponse toOwnerResponse(CollectionEntryEntity entity);

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


    @Mapping(target = "slingMale", source = "request.slingMale")
    @Mapping(target = "slingFemale", source = "request.slingFemale")
    @Mapping(target = "slingUnsexed", source = "request.slingUnsexed")
    @Mapping(target = "juvenileMale", source = "request.juvenileMale")
    @Mapping(target = "juvenileFemale", source = "request.juvenileFemale")
    @Mapping(target = "juvenileUnsexed", source = "request.juvenileUnsexed")
    @Mapping(target = "subadultMale", source = "request.subadultMale")
    @Mapping(target = "subadultFemale", source = "request.subadultFemale")
    @Mapping(target = "subadultUnsexed", source = "request.subadultUnsexed")
    @Mapping(target = "adultMale", source = "request.adultMale")
    @Mapping(target = "adultFemale", source = "request.adultFemale")
    @Mapping(target = "adultUnsexed", source = "request.adultUnsexed")
    @Mapping(target = "featuredImageUrl", source = "request.featuredImageUrl")
    @Mapping(target = "customInfo", source = "request.customInfo")
    UpdateCollectionEntryRequest toUpdateRequest(CollectionEntryEntity entry,
                                                 CreateCollectionEntryRequest request);
}
