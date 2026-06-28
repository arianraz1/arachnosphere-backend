package com.tarantulacommunity.tarantulasocial.features.admin.inat.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InatTaxonTest {

    @Test
    void testNoArgsConstructor() {
        InatTaxon taxon = new InatTaxon();
        assertNotNull(taxon);
        assertNull(taxon.getId());
        assertNull(taxon.getScientificName());
        assertNull(taxon.getCommonName());
        assertNull(taxon.getDefaultPhoto());
    }

    @Test
    void testSettersAndGetters() {
        InatTaxon taxon = new InatTaxon();
        
        taxon.setId(123L);
        taxon.setScientificName("Brachypelma hamorii");
        taxon.setCommonName("Mexican Red Knee");
        
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        photo.setMediumUrl("https://example.com/medium.jpg");
        photo.setAttribution("Photographer Name");
        taxon.setDefaultPhoto(photo);

        assertEquals(123L, taxon.getId());
        assertEquals("Brachypelma hamorii", taxon.getScientificName());
        assertEquals("Mexican Red Knee", taxon.getCommonName());
        assertNotNull(taxon.getDefaultPhoto());
        assertEquals("https://example.com/medium.jpg", taxon.getDefaultPhoto().getMediumUrl());
        assertEquals("Photographer Name", taxon.getDefaultPhoto().getAttribution());
    }

    @Test
    void testInatPhotoNoArgsConstructor() {
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        assertNotNull(photo);
        assertNull(photo.getMediumUrl());
        assertNull(photo.getAttribution());
    }

    @Test
    void testInatPhotoSettersAndGetters() {
        InatTaxon.InatPhoto photo = new InatTaxon.InatPhoto();
        
        photo.setMediumUrl("https://example.com/medium.jpg");
        photo.setAttribution("Photographer Name");

        assertEquals("https://example.com/medium.jpg", photo.getMediumUrl());
        assertEquals("Photographer Name", photo.getAttribution());
    }

    @Test
    void testCanSetNullValues() {
        InatTaxon taxon = new InatTaxon();
        
        taxon.setId(null);
        taxon.setScientificName(null);
        taxon.setCommonName(null);
        taxon.setDefaultPhoto(null);

        assertNull(taxon.getId());
        assertNull(taxon.getScientificName());
        assertNull(taxon.getCommonName());
        assertNull(taxon.getDefaultPhoto());
    }
}
