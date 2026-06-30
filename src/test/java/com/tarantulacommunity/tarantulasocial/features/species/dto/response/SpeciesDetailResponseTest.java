package com.tarantulacommunity.tarantulasocial.features.species.dto.response;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesDetailResponseTest {

    @Test
    void testAllArgsConstructor() {
        SpeciesResponse species = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null);
        CollectionEntryOwnerResponse owner = new CollectionEntryOwnerResponse();
        List<CollectionEntryOwnerResponse> owners = List.of(owner);

        SpeciesDetailResponse response = new SpeciesDetailResponse(species, owners);

        assertEquals(species, response.getSpecies());
        assertEquals(owners, response.getOwners());
    }

    @Test
    void testConstructorWithEmptyOwners() {
        SpeciesResponse species = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null);
        List<CollectionEntryOwnerResponse> emptyOwners = List.of();

        SpeciesDetailResponse response = new SpeciesDetailResponse(species, emptyOwners);

        assertEquals(species, response.getSpecies());
        assertTrue(response.getOwners().isEmpty());
    }

    @Test
    void testConstructorWithNullOwners() {
        SpeciesResponse species = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null);

        SpeciesDetailResponse response = new SpeciesDetailResponse(species, null);

        assertEquals(species, response.getSpecies());
        assertNull(response.getOwners());
    }
}
