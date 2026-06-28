package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GbifDescriptionTest {

    @Test
    void testNoArgsConstructor() {
        GbifDescription description = new GbifDescription();
        assertNotNull(description);
        assertNull(description.getDescription());
        assertNull(description.getLanguage());
    }

    @Test
    void testSettersAndGetters() {
        GbifDescription description = new GbifDescription();
        
        description.setDescription("A beautiful tarantula species");
        description.setLanguage("en");
        
        assertEquals("A beautiful tarantula species", description.getDescription());
        assertEquals("en", description.getLanguage());
    }

    @Test
    void testCanSetNullValues() {
        GbifDescription description = new GbifDescription();
        
        description.setDescription(null);
        description.setLanguage(null);
        
        assertNull(description.getDescription());
        assertNull(description.getLanguage());
    }
}
