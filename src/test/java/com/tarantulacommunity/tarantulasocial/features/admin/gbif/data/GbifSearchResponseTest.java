package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GbifSearchResponseTest {

    @Test
    void testNoArgsConstructor() {
        GbifSearchResponse response = new GbifSearchResponse();
        assertNotNull(response);
        assertNull(response.getResults());
        assertEquals(0, response.getCount());
        assertEquals(0, response.getLimit());
        assertEquals(0, response.getOffset());
        assertFalse(response.isEndOfRecords());
    }

    @Test
    void testSettersAndGetters() {
        GbifSearchResponse response = new GbifSearchResponse();
        List<GbifTaxon> results = List.of(new GbifTaxon());
        
        response.setResults(results);
        response.setCount(100);
        response.setLimit(20);
        response.setOffset(0);
        response.setEndOfRecords(false);
        
        assertEquals(results, response.getResults());
        assertEquals(100, response.getCount());
        assertEquals(20, response.getLimit());
        assertEquals(0, response.getOffset());
        assertFalse(response.isEndOfRecords());
    }

    @Test
    void testEndOfRecordsTrue() {
        GbifSearchResponse response = new GbifSearchResponse();
        response.setEndOfRecords(true);
        
        assertTrue(response.isEndOfRecords());
    }

    @Test
    void testPaginationParameters() {
        GbifSearchResponse response = new GbifSearchResponse();
        
        response.setLimit(50);
        response.setOffset(100);
        
        assertEquals(50, response.getLimit());
        assertEquals(100, response.getOffset());
    }
}
