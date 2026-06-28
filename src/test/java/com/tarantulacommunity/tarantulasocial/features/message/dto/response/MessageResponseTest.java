package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageResponseTest {

    @Test
    void testNoArgsConstructor() {
        MessageResponse response = new MessageResponse();
        assertNotNull(response);
        assertNull(response.getId());
        assertNull(response.getSender());
        assertNull(response.getRecipient());
        assertNull(response.getContent());
        assertNull(response.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        UserSummaryResponse sender = new UserSummaryResponse();
        UserSummaryResponse recipient = new UserSummaryResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        MessageResponse response = new MessageResponse(
            1L, sender, recipient, "Hello", createdAt
        );
        
        assertEquals(1L, response.getId());
        assertEquals(sender, response.getSender());
        assertEquals(recipient, response.getRecipient());
        assertEquals("Hello", response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    void testSettersAndGetters() {
        MessageResponse response = new MessageResponse();
        UserSummaryResponse sender = new UserSummaryResponse();
        UserSummaryResponse recipient = new UserSummaryResponse();
        LocalDateTime createdAt = LocalDateTime.now();
        
        response.setId(1L);
        response.setSender(sender);
        response.setRecipient(recipient);
        response.setContent("Hello");
        response.setCreatedAt(createdAt);
        
        assertEquals(1L, response.getId());
        assertEquals(sender, response.getSender());
        assertEquals(recipient, response.getRecipient());
        assertEquals("Hello", response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
    }
}
