package com.tarantulacommunity.tarantulasocial.features.species.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpeciesSummaryResponseTest {

    @Test
    void testNoArgsConstructor() {
        SpeciesSummaryResponse response = new SpeciesSummaryResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getScientificName());
        assertNull(response.getCommonName());
        assertNull(response.getImageUrl());
    }

    @Test
    void testAllArgsConstructor() {
        SpeciesSummaryResponse response = new SpeciesSummaryResponse(
            1L, "Chromatopelma cyaneopubescens", "Greenbottle Blue", "http://example.com/image.jpg"
        );
        
        assertEquals(1L, response.getId());
        assertEquals("Chromatopelma cyaneopubescens", response.getScientificName());
        assertEquals("Greenbottle Blue", response.getCommonName());
        assertEquals("http://example.com/image.jpg", response.getImageUrl());
    }

    @Test
    void testSettersAndGetters() {
        SpeciesSummaryResponse response = new SpeciesSummaryResponse();
        
        response.setId(1L);
        response.setScientificName("Brachypelma hamorii");
        response.setCommonName("Mexican Red Knee");
        response.setImageUrl("http://example.com/spider.jpg");
        
        assertEquals(1L, response.getId());
        assertEquals("Brachypelma hamorii", response.getScientificName());
        assertEquals("Mexican Red Knee", response.getCommonName());
        assertEquals("http://example.com/spider.jpg", response.getImageUrl());
    }

    @Test
    void testCanSetNullValues() {
        SpeciesSummaryResponse response = new SpeciesSummaryResponse();
        
        response.setId(null);
        response.setScientificName(null);
        response.setCommonName(null);
        response.setImageUrl(null);
        
        assertNull(response.getId());
        assertNull(response.getScientificName());
        assertNull(response.getCommonName());
        assertNull(response.getImageUrl());
    }
}
