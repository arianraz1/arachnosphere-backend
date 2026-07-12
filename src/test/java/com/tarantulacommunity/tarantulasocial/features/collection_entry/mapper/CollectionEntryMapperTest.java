package com.tarantulacommunity.tarantulasocial.features.collection_entry.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionImageEntity;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CollectionEntryMapperTest {

    @Autowired
    private CollectionEntryMapper collectionEntryMapper;

    @Test
    void testToResponse() {
        UserEntity user = TestUtilities.verifiedUser("1", "1");
        user.setId(1L);
        user.setProfilePicKey("profile-pictures/user.jpg");

        SpeciesEntity species = TestUtilities.validSpecies(123456L, "Grammostola rosea");
        species.setId(1L);
        species.setCommonName("Chilean Rosehair");
        species.setImageUrl("https://example.com/species.jpg");

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(1L);
        entry.setSlingMale(2);
        entry.setSlingFemale(3);
        entry.setSlingUnsexed(1);
        entry.setJuvenileMale(1);
        entry.setJuvenileFemale(2);
        entry.setJuvenileUnsexed(0);
        entry.setSubadultMale(0);
        entry.setSubadultFemale(1);
        entry.setSubadultUnsexed(0);
        entry.setAdultMale(0);
        entry.setAdultFemale(1);
        entry.setAdultUnsexed(0);
        entry.setCustomInfo("Beautiful specimens!");

        CollectionEntryResponse response = collectionEntryMapper.toResponse(entry);

        assertNotNull(response);
        assertEquals(1L, response.getId());

        assertNotNull(response.getUser());
        assertEquals(1L, response.getUser().getId());
        assertEquals("test1", response.getUser().getUsername());
        assertEquals("https://tarantula-images-test.s3.us-east-1.amazonaws.com/profile-pictures/user.jpg", response.getUser().getProfilePicUrl());

        assertNotNull(response.getSpecies());
        assertEquals(1L, response.getSpecies().getId());
        assertEquals("Grammostola rosea", response.getSpecies().getScientificName());
        assertEquals("Chilean Rosehair", response.getSpecies().getCommonName());

        assertEquals(2, response.getSlingMale());
        assertEquals(3, response.getSlingFemale());
        assertEquals(1, response.getSlingUnsexed());
        assertEquals(1, response.getJuvenileMale());
        assertEquals(2, response.getJuvenileFemale());
        assertEquals(0, response.getJuvenileUnsexed());
        assertEquals(0, response.getSubadultMale());
        assertEquals(1, response.getSubadultFemale());
        assertEquals(0, response.getSubadultUnsexed());
        assertEquals(0, response.getAdultMale());
        assertEquals(1, response.getAdultFemale());
        assertEquals(0, response.getAdultUnsexed());

        assertEquals("Beautiful specimens!", response.getCustomInfo());
    }

    @Test
    void testToResponse_imagesAreNotMapped_setSeparatelyByService() {
        UserEntity user = TestUtilities.verifiedUser("99", "99");
        user.setId(99L);

        SpeciesEntity species = TestUtilities.validSpecies(999999L, "Grammostola rosea");
        species.setId(99L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(99L);
        entry.getImages().add(CollectionImageEntity.builder()
                .collection(entry)
                .s3Key("collection-images/99/photo.jpg")
                .build());

        CollectionEntryResponse response = collectionEntryMapper.toResponse(entry);

        // @Mapping(target = "images", ignore = true) - CollectionEntryService is
        // responsible for populating this field via CollectionImageService, not the mapper.
        assertNull(response.getImages());
    }

    @Test
    void testToResponseWithAllZeroCounts() {
        UserEntity user = TestUtilities.verifiedUser("2", "2");
        user.setId(2L);

        SpeciesEntity species = TestUtilities.validSpecies(234567L, "Lampropelma violaceopes");
        species.setId(2L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(2L);

        CollectionEntryResponse response = collectionEntryMapper.toResponse(entry);

        assertEquals(0, response.getSlingMale());
        assertEquals(0, response.getSlingFemale());
        assertEquals(0, response.getSlingUnsexed());
        assertEquals(0, response.getJuvenileMale());
        assertEquals(0, response.getJuvenileFemale());
        assertEquals(0, response.getJuvenileUnsexed());
        assertEquals(0, response.getSubadultMale());
        assertEquals(0, response.getSubadultFemale());
        assertEquals(0, response.getSubadultUnsexed());
        assertEquals(0, response.getAdultMale());
        assertEquals(0, response.getAdultFemale());
        assertEquals(0, response.getAdultUnsexed());
    }

    @Test
    void testToResponseWithoutOptionalFields() {
        UserEntity user = TestUtilities.verifiedUser("3", "3");
        user.setId(3L);

        SpeciesEntity species = TestUtilities.validSpecies(345678L, "Avicularia avicularia");
        species.setId(3L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(3L);
        entry.setCustomInfo(null);

        CollectionEntryResponse response = collectionEntryMapper.toResponse(entry);

        assertNotNull(response);
        assertNull(response.getCustomInfo());
    }

    @Test
    void testToEntity() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(1);
        request.setSlingFemale(2);
        request.setSlingUnsexed(0);
        request.setJuvenileMale(1);
        request.setJuvenileFemale(1);
        request.setJuvenileUnsexed(1);
        request.setSubadultMale(0);
        request.setSubadultFemale(1);
        request.setSubadultUnsexed(0);
        request.setAdultMale(1);
        request.setAdultFemale(1);
        request.setAdultUnsexed(0);
        request.setCustomInfo("Custom details");

        CollectionEntryEntity entity = collectionEntryMapper.toEntity(request);

        assertNotNull(entity);
        assertNull(entity.getId());
        assertNull(entity.getUser());
        assertNull(entity.getSpecies());

        assertEquals(1, entity.getSlingMale());
        assertEquals(2, entity.getSlingFemale());
        assertEquals(0, entity.getSlingUnsexed());
        assertEquals(1, entity.getJuvenileMale());
        assertEquals(1, entity.getJuvenileFemale());
        assertEquals(1, entity.getJuvenileUnsexed());
        assertEquals(0, entity.getSubadultMale());
        assertEquals(1, entity.getSubadultFemale());
        assertEquals(0, entity.getSubadultUnsexed());
        assertEquals(1, entity.getAdultMale());
        assertEquals(1, entity.getAdultFemale());
        assertEquals(0, entity.getAdultUnsexed());

        assertEquals("Custom details", entity.getCustomInfo());
    }

    @Test
    void testToEntityIgnoresImmutableFields() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(2);

        CollectionEntryEntity entity = collectionEntryMapper.toEntity(request);

        assertNull(entity.getId());
        assertNull(entity.getUser());
        assertNull(entity.getSpecies());
        assertNull(entity.getVersion());
    }

    @Test
    void testUpdateEntityFromRequest() {
        UserEntity user = TestUtilities.verifiedUser("4", "4");
        user.setId(4L);

        SpeciesEntity species = TestUtilities.validSpecies(456789L, "Brachypelma smithi");
        species.setId(4L);

        CollectionEntryEntity entity = TestUtilities.validCollectionEntry(user, species);
        entity.setId(4L);
        entity.setVersion(0L);
        entity.setSlingMale(1);
        entity.setAdultFemale(1);

        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(3);
        request.setSlingFemale(2);
        request.setSlingUnsexed(1);
        request.setJuvenileMale(0);
        request.setJuvenileFemale(0);
        request.setJuvenileUnsexed(0);
        request.setSubadultMale(0);
        request.setSubadultFemale(0);
        request.setSubadultUnsexed(0);
        request.setAdultMale(0);
        request.setAdultFemale(2);
        request.setAdultUnsexed(0);
        request.setCustomInfo("Updated info");

        collectionEntryMapper.updateEntityFromRequest(request, entity);

        assertEquals(3, entity.getSlingMale());
        assertEquals(2, entity.getSlingFemale());
        assertEquals(1, entity.getSlingUnsexed());
        assertEquals(2, entity.getAdultFemale());
        assertEquals("Updated info", entity.getCustomInfo());

        assertEquals(4L, entity.getId());
        assertEquals(user, entity.getUser());
        assertEquals(species, entity.getSpecies());
        assertEquals(0L, entity.getVersion());
    }

    @Test
    void testUpdateEntityFromRequestIgnoresImmutableFields() {
        UserEntity user = TestUtilities.verifiedUser("5", "5");
        user.setId(5L);

        SpeciesEntity species = TestUtilities.validSpecies(567890L, "Poecilotheria striata");
        species.setId(5L);

        CollectionEntryEntity entity = TestUtilities.validCollectionEntry(user, species);
        entity.setId(5L);
        Long originalId = entity.getId();
        UserEntity originalUser = entity.getUser();
        SpeciesEntity originalSpecies = entity.getSpecies();
        Long originalVersion = entity.getVersion();

        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setAdultMale(1);

        collectionEntryMapper.updateEntityFromRequest(request, entity);

        assertEquals(originalId, entity.getId());
        assertEquals(originalUser, entity.getUser());
        assertEquals(originalSpecies, entity.getSpecies());
        assertEquals(originalVersion, entity.getVersion());
    }

    @Test
    void testToUpdateRequest() {
        UserEntity user = TestUtilities.verifiedUser("6", "6");
        user.setId(6L);

        SpeciesEntity species = TestUtilities.validSpecies(678901L, "Teraphosa blondi");
        species.setId(6L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setSlingMale(2);
        entry.setSlingFemale(1);
        entry.setJuvenileMale(1);
        entry.setAdultFemale(2);
        entry.setCustomInfo("Goliath Birdeater");

        CreateCollectionEntryRequest createRequest = new CreateCollectionEntryRequest();
        createRequest.setSlingMale(2);
        createRequest.setSlingFemale(2);
        createRequest.setCustomInfo("New info");

        UpdateCollectionEntryRequest updateRequest = collectionEntryMapper.toUpdateRequest(entry, createRequest);

        assertNotNull(updateRequest);
        assertEquals(2, updateRequest.getSlingMale());
        assertEquals(2, updateRequest.getSlingFemale());
        assertEquals("New info", updateRequest.getCustomInfo());
    }

    @Test
    void testToUpdateRequestWithNullOptionalFields() {
        UserEntity user = TestUtilities.verifiedUser("7", "7");
        user.setId(7L);

        SpeciesEntity species = TestUtilities.validSpecies(789012L, "Ceratogyrus darlingi");
        species.setId(7L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);

        CreateCollectionEntryRequest createRequest = new CreateCollectionEntryRequest();
        createRequest.setAdultMale(1);

        UpdateCollectionEntryRequest updateRequest = collectionEntryMapper.toUpdateRequest(entry, createRequest);

        assertNotNull(updateRequest);
        assertNull(updateRequest.getCustomInfo());
    }

    @Test
    void testComplexMappingWithHighQuantities() {
        UserEntity user = TestUtilities.verifiedUser("8", "8");
        user.setId(8L);

        SpeciesEntity species = TestUtilities.validSpecies(890123L, "Species with many individuals");
        species.setId(8L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(8L);
        entry.setSlingMale(10);
        entry.setSlingFemale(15);
        entry.setSlingUnsexed(5);
        entry.setJuvenileMale(3);
        entry.setJuvenileFemale(7);
        entry.setJuvenileUnsexed(2);
        entry.setSubadultMale(1);
        entry.setSubadultFemale(3);
        entry.setSubadultUnsexed(1);
        entry.setAdultMale(1);
        entry.setAdultFemale(2);
        entry.setAdultUnsexed(1);

        CollectionEntryResponse response = collectionEntryMapper.toResponse(entry);

        assertEquals(10, response.getSlingMale());
        assertEquals(15, response.getSlingFemale());
        assertEquals(5, response.getSlingUnsexed());
        assertEquals(3, response.getJuvenileMale());
        assertEquals(7, response.getJuvenileFemale());
        assertEquals(2, response.getJuvenileUnsexed());
        assertEquals(1, response.getSubadultMale());
        assertEquals(3, response.getSubadultFemale());
        assertEquals(1, response.getSubadultUnsexed());
        assertEquals(1, response.getAdultMale());
        assertEquals(2, response.getAdultFemale());
        assertEquals(1, response.getAdultUnsexed());
    }

    @Test
    void testToOwnerResponse_shouldMapEntityToOwnerDto() {
        UserEntity user = TestUtilities.verifiedUser("9", "9");
        user.setId(9L);
        user.setProfilePicKey("profile-pictures/owner-avatar.jpg");

        SpeciesEntity species = TestUtilities.validSpecies(901234L, "Lasiodora parahybana");
        species.setId(9L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(9L);
        entry.setSlingMale(10);
        entry.setSlingFemale(5);
        entry.setSlingUnsexed(2);
        entry.setJuvenileMale(1);
        entry.setJuvenileFemale(3);
        entry.setJuvenileUnsexed(0);
        entry.setSubadultMale(0);
        entry.setSubadultFemale(0);
        entry.setSubadultUnsexed(0);
        entry.setAdultMale(0);
        entry.setAdultFemale(1);
        entry.setAdultUnsexed(0);
        entry.setCustomInfo("This is my favorite tarantula!");

        CollectionEntryOwnerResponse response = collectionEntryMapper.toOwnerResponse(entry);

        assertNotNull(response);
        assertEquals(9L, response.getId());
        assertEquals("test9", response.getUser().getUsername());
        assertEquals(9L, response.getUser().getId());
        assertEquals("https://tarantula-images-test.s3.us-east-1.amazonaws.com/profile-pictures/owner-avatar.jpg", response.getUser().getProfilePicUrl());

        assertEquals(10, response.getSlingMale());
        assertEquals(5, response.getSlingFemale());
        assertEquals(2, response.getSlingUnsexed());
        assertEquals(1, response.getJuvenileMale());
        assertEquals(3, response.getJuvenileFemale());
        assertEquals(0, response.getJuvenileUnsexed());
        assertEquals(0, response.getSubadultMale());
        assertEquals(0, response.getSubadultFemale());
        assertEquals(0, response.getSubadultUnsexed());
        assertEquals(0, response.getAdultMale());
        assertEquals(1, response.getAdultFemale());
        assertEquals(0, response.getAdultUnsexed());

        assertEquals("This is my favorite tarantula!", response.getCustomInfo());
    }

    @Test
    void testToOwnerResponse_imagesAreNotMapped_setSeparatelyByService() {
        UserEntity user = TestUtilities.verifiedUser("10", "10");
        user.setId(10L);

        SpeciesEntity species = TestUtilities.validSpecies(101010L, "Lasiodora parahybana");
        species.setId(10L);

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(10L);
        entry.getImages().add(CollectionImageEntity.builder()
                .collection(entry)
                .s3Key("collection-images/10/photo.jpg")
                .build());

        CollectionEntryOwnerResponse response = collectionEntryMapper.toOwnerResponse(entry);

        assertNull(response.getImages());
    }
}