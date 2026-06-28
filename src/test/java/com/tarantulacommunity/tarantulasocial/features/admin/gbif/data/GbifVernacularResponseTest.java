package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GbifVernacularResponseTest {

    @Test
    void testNoArgsConstructor() {
        GbifVernacularResponse response = new GbifVernacularResponse();
        assertNotNull(response);
        assertNull(response.getResults());
        assertFalse(response.isEndOfRecords());
        assertEquals(0, response.getLimit());
        assertEquals(0, response.getOffset());
    }

    @Test
    void testSettersAndGetters() {
        GbifVernacularResponse response = new GbifVernacularResponse();
        List<GbifVernacular> results = List.of(new GbifVernacular());
        
        response.setResults(results);
        response.setEndOfRecords(true);
        response.setLimit(20);
        response.setOffset(0);
        
        assertEquals(results, response.getResults());
        assertTrue(response.isEndOfRecords());
        assertEquals(20, response.getLimit());
        assertEquals(0, response.getOffset());
    }

    @Test
    void testEndOfRecordsTrue() {
        GbifVernacularResponse response = new GbifVernacularResponse();
        response.setEndOfRecords(true);
        
        assertTrue(response.isEndOfRecords());
    }

    @Test
    void testEndOfRecordsFalse() {
        GbifVernacularResponse response = new GbifVernacularResponse();
        response.setEndOfRecords(false);
        
        assertFalse(response.isEndOfRecords());
    }

    @Test
    void testPaginationParameters() {
        GbifVernacularResponse response = new GbifVernacularResponse();
        
        response.setLimit(30);
        response.setOffset(60);
        
        assertEquals(30, response.getLimit());
        assertEquals(60, response.getOffset());
    }
}
