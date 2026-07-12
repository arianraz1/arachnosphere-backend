package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response;

import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionEntryOwnerResponseTest {

    @Test
    void testNoArgsConstructor() {
        CollectionEntryOwnerResponse response = new CollectionEntryOwnerResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUser());
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
        assertNull(response.getCustomInfo());
        assertNull(response.getImages());
    }

    @Test
    void testAllArgsConstructor() {
        UserSummaryResponse user = new UserSummaryResponse();
        List<CollectionImageResponse> images = List.of(
                new CollectionImageResponse(1L, "http://example.com/image.jpg", "caption", true, 0));

        CollectionEntryOwnerResponse response = new CollectionEntryOwnerResponse(
                1L, user,
                1, 2, 3,
                4, 5, 6,
                7, 8, 9,
                10, 11, 12,
                "Custom info",
                images
        );

        assertEquals(1L, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(1, response.getSlingMale());
        assertEquals(2, response.getSlingFemale());
        assertEquals(3, response.getSlingUnsexed());
        assertEquals(4, response.getJuvenileMale());
        assertEquals(5, response.getJuvenileFemale());
        assertEquals(6, response.getJuvenileUnsexed());
        assertEquals(7, response.getSubadultMale());
        assertEquals(8, response.getSubadultFemale());
        assertEquals(9, response.getSubadultUnsexed());
        assertEquals(10, response.getAdultMale());
        assertEquals(11, response.getAdultFemale());
        assertEquals(12, response.getAdultUnsexed());
        assertEquals("Custom info", response.getCustomInfo());
        assertEquals(images, response.getImages());
    }

    @Test
    void testSettersAndGetters() {
        CollectionEntryOwnerResponse response = new CollectionEntryOwnerResponse();
        UserSummaryResponse user = new UserSummaryResponse();
        List<CollectionImageResponse> images = List.of(
                new CollectionImageResponse(2L, "http://example.com/image2.jpg", null, false, 1));

        response.setId(1L);
        response.setUser(user);
        response.setSlingMale(1);
        response.setSlingFemale(2);
        response.setSlingUnsexed(3);
        response.setJuvenileMale(4);
        response.setJuvenileFemale(5);
        response.setJuvenileUnsexed(6);
        response.setSubadultMale(7);
        response.setSubadultFemale(8);
        response.setSubadultUnsexed(9);
        response.setAdultMale(10);
        response.setAdultFemale(11);
        response.setAdultUnsexed(12);
        response.setCustomInfo("Custom info");
        response.setImages(images);

        assertEquals(1L, response.getId());
        assertEquals(user, response.getUser());
        assertEquals(1, response.getSlingMale());
        assertEquals(2, response.getSlingFemale());
        assertEquals(3, response.getSlingUnsexed());
        assertEquals(4, response.getJuvenileMale());
        assertEquals(5, response.getJuvenileFemale());
        assertEquals(6, response.getJuvenileUnsexed());
        assertEquals(7, response.getSubadultMale());
        assertEquals(8, response.getSubadultFemale());
        assertEquals(9, response.getSubadultUnsexed());
        assertEquals(10, response.getAdultMale());
        assertEquals(11, response.getAdultFemale());
        assertEquals(12, response.getAdultUnsexed());
        assertEquals("Custom info", response.getCustomInfo());
        assertEquals(images, response.getImages());
    }

    @Test
    void testEmptyImagesList() {
        CollectionEntryOwnerResponse response = new CollectionEntryOwnerResponse();
        response.setImages(List.of());

        assertNotNull(response.getImages());
        assertTrue(response.getImages().isEmpty());
    }
}
