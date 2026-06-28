package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GbifTaxonTest {

    @Test
    void testNoArgsConstructor() {
        GbifTaxon taxon = new GbifTaxon();
        assertNotNull(taxon);
        assertNull(taxon.getKey());
        assertNull(taxon.getScientificName());
        assertNull(taxon.getVernacularName());
        assertNull(taxon.getDescriptions());
        assertNull(taxon.getMedia());
    }

    @Test
    void testSettersAndGetters() {
        GbifTaxon taxon = new GbifTaxon();
        List<GbifDescription> descriptions = List.of(new GbifDescription());
        List<GbifMedia> media = List.of(new GbifMedia());
        
        taxon.setKey(12345L);
        taxon.setScientificName("Brachypelma hamorii");
        taxon.setVernacularName("Mexican Red Knee");
        taxon.setDescriptions(descriptions);
        taxon.setMedia(media);
        
        assertEquals(12345L, taxon.getKey());
        assertEquals("Brachypelma hamorii", taxon.getScientificName());
        assertEquals("Mexican Red Knee", taxon.getVernacularName());
        assertEquals(descriptions, taxon.getDescriptions());
        assertEquals(media, taxon.getMedia());
    }

    @Test
    void testEmptyLists() {
        GbifTaxon taxon = new GbifTaxon();
        
        taxon.setDescriptions(List.of());
        taxon.setMedia(List.of());
        
        assertTrue(taxon.getDescriptions().isEmpty());
        assertTrue(taxon.getMedia().isEmpty());
    }

    @Test
    void testScientificName() {
        GbifTaxon taxon = new GbifTaxon();
        taxon.setScientificName("Aphonopelma chalcodes");
        
        assertEquals("Aphonopelma chalcodes", taxon.getScientificName());
    }
}
