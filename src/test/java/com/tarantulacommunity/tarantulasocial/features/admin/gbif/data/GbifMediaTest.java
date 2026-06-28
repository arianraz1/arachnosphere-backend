package com.tarantulacommunity.tarantulasocial.features.admin.gbif.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GbifMediaTest {

    @Test
    void testNoArgsConstructor() {
        GbifMedia media = new GbifMedia();
        assertNotNull(media);
        assertNull(media.getType());
        assertNull(media.getIdentifier());
    }

    @Test
    void testSettersAndGetters() {
        GbifMedia media = new GbifMedia();
        
        media.setType("StillImage");
        media.setIdentifier("http://example.com/spider.jpg");
        
        assertEquals("StillImage", media.getType());
        assertEquals("http://example.com/spider.jpg", media.getIdentifier());
    }

    @Test
    void testTypeIsStillImage() {
        GbifMedia media = new GbifMedia();
        media.setType("StillImage");
        
        assertEquals("StillImage", media.getType());
    }

    @Test
    void testIdentifierIsUrl() {
        GbifMedia media = new GbifMedia();
        media.setIdentifier("https://gbif.org/image.jpg");
        
        assertEquals("https://gbif.org/image.jpg", media.getIdentifier());
    }
}
