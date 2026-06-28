package com.tarantulacommunity.tarantulasocial.features.species.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesResponseTest {

    @Test
    void testNoArgsConstructor() {
        SpeciesResponse response = new SpeciesResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getGbifNubKey());
        assertNull(response.getScientificName());
        assertNull(response.getCommonName());
        assertNull(response.getImageUrl());
        assertNull(response.getDescription());
    }

    @Test
    void testAllArgsConstructor() {
        SpeciesResponse response = new SpeciesResponse(
            1L, 12345L, "Brachypelma hamorii", "Mexican Red Knee",
            "http://example.com/spider.jpg", "A beautiful tarantula"
        );
        
        assertEquals(1L, response.getId());
        assertEquals(12345L, response.getGbifNubKey());
        assertEquals("Brachypelma hamorii", response.getScientificName());
        assertEquals("Mexican Red Knee", response.getCommonName());
        assertEquals("http://example.com/spider.jpg", response.getImageUrl());
        assertEquals("A beautiful tarantula", response.getDescription());
    }

    @Test
    void testSettersAndGetters() {
        SpeciesResponse response = new SpeciesResponse();
        
        response.setId(1L);
        response.setGbifNubKey(12345L);
        response.setScientificName("Brachypelma hamorii");
        response.setCommonName("Mexican Red Knee");
        response.setImageUrl("http://example.com/spider.jpg");
        response.setDescription("A beautiful tarantula");
        
        assertEquals(1L, response.getId());
        assertEquals(12345L, response.getGbifNubKey());
        assertEquals("Brachypelma hamorii", response.getScientificName());
        assertEquals("Mexican Red Knee", response.getCommonName());
        assertEquals("http://example.com/spider.jpg", response.getImageUrl());
        assertEquals("A beautiful tarantula", response.getDescription());
    }
}
