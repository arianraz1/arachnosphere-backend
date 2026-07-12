package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CollectionImageResponseTest {

    @Test
    void testNoArgsConstructor() {
        CollectionImageResponse response = new CollectionImageResponse();

        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getUrl());
        assertNull(response.getCaption());
        assertFalse(response.isFeatured());
        assertEquals(0, response.getDisplayOrder());
    }

    @Test
    void testAllArgsConstructor() {
        CollectionImageResponse response = new CollectionImageResponse(
                1L, "https://example.com/image.jpg", "Molted yesterday", true, 2);

        assertEquals(1L, response.getId());
        assertEquals("https://example.com/image.jpg", response.getUrl());
        assertEquals("Molted yesterday", response.getCaption());
        assertTrue(response.isFeatured());
        assertEquals(2, response.getDisplayOrder());
    }

    @Test
    void testAllArgsConstructor_withNullCaption() {
        CollectionImageResponse response = new CollectionImageResponse(
                2L, "https://example.com/image2.jpg", null, false, 0);

        assertNull(response.getCaption());
    }

    @Test
    void testSettersAndGetters() {
        CollectionImageResponse response = new CollectionImageResponse();

        response.setId(5L);
        response.setUrl("https://example.com/photo.jpg");
        response.setCaption("Fresh molt");
        response.setFeatured(true);
        response.setDisplayOrder(3);

        assertEquals(5L, response.getId());
        assertEquals("https://example.com/photo.jpg", response.getUrl());
        assertEquals("Fresh molt", response.getCaption());
        assertTrue(response.isFeatured());
        assertEquals(3, response.getDisplayOrder());
    }

    @Test
    void testEquals_defaultsToIdentity_sinceNoEqualsAndHashCodeAnnotation() {
        // CollectionImageResponse only has @Getter/@Setter/@NoArgsConstructor/@AllArgsConstructor,
        // no @EqualsAndHashCode or @Data — so equals() is reference equality, not value equality.
        CollectionImageResponse a = new CollectionImageResponse(1L, "url", "cap", true, 0);
        CollectionImageResponse b = new CollectionImageResponse(1L, "url", "cap", true, 0);

        assertNotEquals(a, b);
        assertEquals(a, a);
    }
}
