package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GbifMatchResponseTest {

    @Test
    void testNoArgsConstructor() {
        GbifMatchResponse response = new GbifMatchResponse();
        assertNotNull(response);
        assertNull(response.getUsageKey());
        assertNull(response.getScientificName());
        assertNull(response.getRank());
        assertNull(response.getMatchType());
    }

    @Test
    void testSettersAndGetters() {
        GbifMatchResponse response = new GbifMatchResponse();
        
        response.setUsageKey(12345L);
        response.setScientificName("Brachypelma hamorii");
        response.setRank("Species");
        response.setMatchType("EXACT");
        
        assertEquals(12345L, response.getUsageKey());
        assertEquals("Brachypelma hamorii", response.getScientificName());
        assertEquals("Species", response.getRank());
        assertEquals("EXACT", response.getMatchType());
    }

    @Test
    void testMatchTypeValues() {
        GbifMatchResponse response = new GbifMatchResponse();
        
        response.setMatchType("EXACT");
        assertEquals("EXACT", response.getMatchType());
        
        response.setMatchType("FUZZY");
        assertEquals("FUZZY", response.getMatchType());
        
        response.setMatchType("NONE");
        assertEquals("NONE", response.getMatchType());
    }
}
