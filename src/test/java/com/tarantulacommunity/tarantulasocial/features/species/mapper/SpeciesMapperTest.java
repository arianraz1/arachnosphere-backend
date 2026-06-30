package com.tarantulacommunity.tarantulasocial.features.species.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesDetailResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SpeciesMapperTest {

    @Autowired
    private SpeciesMapper speciesMapper;

    @Test
    void testToResponse() {
        SpeciesEntity species = TestUtilities.validSpecies(123456L, "Psalmopoeus irminia");
        species.setId(1L);
        species.setCommonName("Tailless Whip Scorpion");
        species.setImageUrl("https://example.com/tailless-whip.jpg");
        species.setDescription("A fascinating small arachnid known for its unique appearance");

        SpeciesResponse response = speciesMapper.toResponse(species);

        assertNotNull(response);
        assertEquals(species.getId(), response.getId());
        assertEquals(species.getGbifNubKey(), response.getGbifNubKey());
        assertEquals(species.getScientificName(), response.getScientificName());
        assertEquals(species.getCommonName(), response.getCommonName());
        assertEquals(species.getImageUrl(), response.getImageUrl());
        assertEquals(species.getDescription(), response.getDescription());
    }

    @Test
    void testToResponseWithMinimalData() {
        SpeciesEntity species = TestUtilities.validSpecies(789012L, "Grammostola rosea");
        species.setId(2L);

        SpeciesResponse response = speciesMapper.toResponse(species);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals(789012L, response.getGbifNubKey());
        assertEquals("Grammostola rosea", response.getScientificName());
        assertNull(response.getCommonName());
        assertNull(response.getImageUrl());
        assertNull(response.getDescription());
    }

    @Test
    void testToResponseWithAllFields() {
        SpeciesEntity species = new SpeciesEntity();
        species.setId(3L);
        species.setGbifNubKey(345678L);
        species.setScientificName("Acanthoscurria geniculata");
        species.setCommonName("Brazilian Whiteknee Tarantula");
        species.setImageUrl("https://example.com/brazilian-whiteknee.jpg");
        species.setDescription("A large and popular tarantula species from Brazil, known for its docile temperament");

        SpeciesResponse response = speciesMapper.toResponse(species);

        assertEquals(3L, response.getId());
        assertEquals(345678L, response.getGbifNubKey());
        assertEquals("Acanthoscurria geniculata", response.getScientificName());
        assertEquals("Brazilian Whiteknee Tarantula", response.getCommonName());
        assertEquals("https://example.com/brazilian-whiteknee.jpg", response.getImageUrl());
        assertEquals("A large and popular tarantula species from Brazil, known for its docile temperament",
                response.getDescription());
    }

    @Test
    void testToSummaryResponse() {
        SpeciesEntity species = TestUtilities.validSpecies(234567L, "Lampropelma violaceopes");
        species.setId(4L);
        species.setCommonName("Singapore Blue");
        species.setImageUrl("https://example.com/singapore-blue.jpg");

        SpeciesSummaryResponse response = speciesMapper.toSummaryResponse(species);

        assertNotNull(response);
        assertEquals(species.getId(), response.getId());
        assertEquals(species.getScientificName(), response.getScientificName());
        assertEquals(species.getCommonName(), response.getCommonName());
        assertEquals(species.getImageUrl(), response.getImageUrl());
    }

    @Test
    void testToSummaryResponseWithoutOptionalFields() {
        SpeciesEntity species = TestUtilities.validSpecies(567890L, "Poecilotheria striata");
        species.setId(5L);

        SpeciesSummaryResponse response = speciesMapper.toSummaryResponse(species);

        assertNotNull(response);
        assertEquals(5L, response.getId());
        assertEquals("Poecilotheria striata", response.getScientificName());
        assertNull(response.getCommonName());
        assertNull(response.getImageUrl());
    }

    @Test
    void testToSummaryResponseWithAllFields() {
        SpeciesEntity species = new SpeciesEntity();
        species.setId(6L);
        species.setScientificName("Omothymus violipes");
        species.setCommonName("Singapore Blue");
        species.setImageUrl("https://example.com/singapore-blue-alt.jpg");
        species.setDescription("One of the most beautiful tarantulas with vibrant blue coloration");

        SpeciesSummaryResponse response = speciesMapper.toSummaryResponse(species);

        assertEquals(6L, response.getId());
        assertEquals("Omothymus violipes", response.getScientificName());
        assertEquals("Singapore Blue", response.getCommonName());
        assertEquals("https://example.com/singapore-blue-alt.jpg", response.getImageUrl());
    }

    @Test
    void testToResponseMapsScientificNameCorrectly() {
        SpeciesEntity species = TestUtilities.validSpecies(111111L, "Avicularia avicularia");
        species.setId(7L);
        species.setCommonName("Pink Toe Tarantula");

        SpeciesResponse response = speciesMapper.toResponse(species);

        assertEquals("Avicularia avicularia", response.getScientificName());
    }

    @Test
    void testToSummaryResponseMapsScientificNameCorrectly() {
        SpeciesEntity species = TestUtilities.validSpecies(222222L, "Brachypelma smithi");
        species.setId(8L);
        species.setCommonName("Red Knee Tarantula");

        SpeciesSummaryResponse response = speciesMapper.toSummaryResponse(species);

        assertEquals("Brachypelma smithi", response.getScientificName());
    }

    @Test
    void testToResponsePreservesGbifKey() {
        long gbifKey = 3654382L;
        SpeciesEntity species = TestUtilities.validSpecies(gbifKey, "Test Species");
        species.setId(9L);

        SpeciesResponse response = speciesMapper.toResponse(species);

        assertEquals(gbifKey, response.getGbifNubKey());
    }

    @Test
    void testMultipleResponsesHaveDifferentData() {
        SpeciesEntity species1 = TestUtilities.validSpecies(100001L, "Species One");
        species1.setId(10L);
        species1.setCommonName("One");

        SpeciesEntity species2 = TestUtilities.validSpecies(100002L, "Species Two");
        species2.setId(11L);
        species2.setCommonName("Two");

        SpeciesResponse response1 = speciesMapper.toResponse(species1);
        SpeciesResponse response2 = speciesMapper.toResponse(species2);

        assertNotEquals(response1.getId(), response2.getId());
        assertNotEquals(response1.getGbifNubKey(), response2.getGbifNubKey());
        assertNotEquals(response1.getScientificName(), response2.getScientificName());
    }

    @Test
    void testToDetailResponse_shouldCombineSpeciesAndOwners() {
        // 1. Create SpeciesEntity using TestUtilities
        SpeciesEntity speciesEntity = TestUtilities.validSpecies(123456L, "Brachypelma smithi");
        speciesEntity.setId(1L);
        speciesEntity.setCommonName("Mexican Red Knee");
        speciesEntity.setImageUrl("https://example.com/smithi.jpg");
        speciesEntity.setDescription("A beautiful tarantula species from Mexico.");

        SpeciesResponse species = speciesMapper.toResponse(speciesEntity);

        UserEntity user = TestUtilities.validUser("1", "dockeruser");
        user.setId(1L);
        user.setProfilePicUrl("https://example.com/avatar.jpg");

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, speciesEntity);
        entry.setId(10L);
        entry.setSlingMale(2);
        entry.setSlingFemale(3);
        entry.setSlingUnsexed(1);
        entry.setJuvenileMale(0);
        entry.setJuvenileFemale(1);
        entry.setJuvenileUnsexed(0);
        entry.setSubadultMale(0);
        entry.setSubadultFemale(0);
        entry.setSubadultUnsexed(0);
        entry.setAdultMale(1);
        entry.setAdultFemale(1);
        entry.setAdultUnsexed(0);
        entry.setFeaturedImageUrl("https://example.com/owner-featured.jpg");
        entry.setCustomInfo("This is my favorite tarantula!");

        CollectionEntryOwnerResponse owner = new CollectionEntryOwnerResponse();
        owner.setId(entry.getId());

        UserSummaryResponse userSummary = new UserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getProfilePicUrl()
        );
        owner.setUser(userSummary);
        owner.setSlingMale(entry.getSlingMale());
        owner.setSlingFemale(entry.getSlingFemale());
        owner.setSlingUnsexed(entry.getSlingUnsexed());
        owner.setJuvenileMale(entry.getJuvenileMale());
        owner.setJuvenileFemale(entry.getJuvenileFemale());
        owner.setJuvenileUnsexed(entry.getJuvenileUnsexed());
        owner.setSubadultMale(entry.getSubadultMale());
        owner.setSubadultFemale(entry.getSubadultFemale());
        owner.setSubadultUnsexed(entry.getSubadultUnsexed());
        owner.setAdultMale(entry.getAdultMale());
        owner.setAdultFemale(entry.getAdultFemale());
        owner.setAdultUnsexed(entry.getAdultUnsexed());
        owner.setFeaturedImageUrl(entry.getFeaturedImageUrl());
        owner.setCustomInfo(entry.getCustomInfo());

        List<CollectionEntryOwnerResponse> owners = List.of(owner);

        SpeciesDetailResponse detail = speciesMapper.toDetailResponse(species, owners);

        assertNotNull(detail);
        assertEquals(species, detail.getSpecies());
        assertEquals(owners, detail.getOwners());
        assertEquals(1, detail.getOwners().size());
        assertEquals("testdockeruser", detail.getOwners().get(0).getUser().getUsername());
        assertEquals(2, detail.getOwners().get(0).getSlingMale());
    }
}


