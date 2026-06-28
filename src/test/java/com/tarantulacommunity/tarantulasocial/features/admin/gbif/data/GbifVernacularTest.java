package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GbifVernacularTest {

    @Test
    void testNoArgsConstructor() {
        GbifVernacular vernacular = new GbifVernacular();
        assertNotNull(vernacular);
        assertNull(vernacular.getVernacularName());
        assertNull(vernacular.getLanguage());
        assertNull(vernacular.getPreferred());
    }

    @Test
    void testSettersAndGetters() {
        GbifVernacular vernacular = new GbifVernacular();
        
        vernacular.setVernacularName("Mexican Red Knee");
        vernacular.setLanguage("en");
        vernacular.setPreferred(true);
        
        assertEquals("Mexican Red Knee", vernacular.getVernacularName());
        assertEquals("en", vernacular.getLanguage());
        assertTrue(vernacular.getPreferred());
    }

    @Test
    void testPreferredTrue() {
        GbifVernacular vernacular = new GbifVernacular();
        vernacular.setPreferred(true);
        
        assertTrue(vernacular.getPreferred());
    }

    @Test
    void testPreferredFalse() {
        GbifVernacular vernacular = new GbifVernacular();
        vernacular.setPreferred(false);
        
        assertFalse(vernacular.getPreferred());
    }

    @Test
    void testLanguageCode() {
        GbifVernacular vernacular = new GbifVernacular();
        vernacular.setLanguage("es");
        
        assertEquals("es", vernacular.getLanguage());
    }
}
