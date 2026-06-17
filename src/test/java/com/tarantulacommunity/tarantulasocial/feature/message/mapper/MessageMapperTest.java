package com.tarantulacommunity.tarantulasocial.feature.message.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Test
    void testToResponse() {
        UserEntity sender = TestUtilities.validUser("1", "1");
        sender.setId(1L);
        sender.setProfilePicUrl("https://example.com/sender.jpg");

        UserEntity recipient = TestUtilities.validUser("2", "2");
        recipient.setId(2L);
        recipient.setProfilePicUrl("https://example.com/recipient.jpg");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hello!");
        message.setId(1L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertEquals(message.getId(), response.getId());
        assertEquals(message.getContent(), response.getContent());
        assertEquals(message.getCreatedAt(), response.getCreatedAt());

        assertNotNull(response.getSender());
        assertEquals(sender.getId(), response.getSender().getId());
        assertEquals(sender.getUsername(), response.getSender().getUsername());
        assertEquals(sender.getProfilePicUrl(), response.getSender().getProfilePicUrl());

        assertNotNull(response.getRecipient());
        assertEquals(recipient.getId(), response.getRecipient().getId());
        assertEquals(recipient.getUsername(), response.getRecipient().getUsername());
        assertEquals(recipient.getProfilePicUrl(), response.getRecipient().getProfilePicUrl());
    }

    @Test
    void testToResponseWithLongMessage() {
        UserEntity sender = TestUtilities.validUser("3", "3");
        sender.setId(3L);

        UserEntity recipient = TestUtilities.validUser("4", "4");
        recipient.setId(4L);

        String longContent = "This is a very long message about tarantulas. " +
                "I have many species in my collection including the Brazilian Black, " +
                "Singapore Blue, and Chilean Rose Hair. They are amazing!";

        MessageEntity message = TestUtilities.validMessage(sender, recipient, longContent);
        message.setId(2L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertEquals(longContent, response.getContent());
        assertEquals(message.getId(), response.getId());
    }

    @Test
    void testToResponsePreservesTimestamp() {
        UserEntity sender = TestUtilities.validUser("5", "5");
        sender.setId(5L);

        UserEntity recipient = TestUtilities.validUser("6", "6");
        recipient.setId(6L);

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Test");
        message.setId(3L);
        LocalDateTime timestamp = LocalDateTime.of(2025, 6, 15, 14, 30, 0);
        message.setCreatedAt(timestamp);

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals(timestamp, response.getCreatedAt());
    }

    @Test
    void testToResponseWithDifferentUsers() {
        UserEntity sender = TestUtilities.validUser("7", "7");
        sender.setId(7L);
        sender.setProfilePicUrl("https://example.com/sender7.jpg");

        UserEntity recipient = TestUtilities.validUser("8", "8");
        recipient.setId(8L);
        recipient.setProfilePicUrl("https://example.com/recipient8.jpg");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Another message");
        message.setId(4L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertEquals("test7", response.getSender().getUsername());
        assertEquals("test8", response.getRecipient().getUsername());
        assertEquals("https://example.com/sender7.jpg", response.getSender().getProfilePicUrl());
        assertEquals("https://example.com/recipient8.jpg", response.getRecipient().getProfilePicUrl());
    }

    @Test
    void testToResponseWithoutProfilePictures() {
        UserEntity sender = TestUtilities.validUser("9", "9");
        sender.setId(9L);

        UserEntity recipient = TestUtilities.validUser("10", "10");
        recipient.setId(10L);

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Message without pics");
        message.setId(5L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertNull(response.getSender().getProfilePicUrl());
        assertNull(response.getRecipient().getProfilePicUrl());
    }

    @Test
    void testToResponseMapsAllFields() {
        UserEntity sender = TestUtilities.validUser("11", "11");
        sender.setId(11L);
        sender.setProfilePicUrl("https://example.com/sender11.jpg");

        UserEntity recipient = TestUtilities.validUser("12", "12");
        recipient.setId(12L);
        recipient.setProfilePicUrl("https://example.com/recipient12.jpg");

        MessageEntity message = new MessageEntity();
        message.setId(6L);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("Complete message test");
        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 25, 12, 0, 0);
        message.setCreatedAt(createdAt);

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals(6L, response.getId());
        assertEquals("Complete message test", response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(11L, response.getSender().getId());
        assertEquals("test11", response.getSender().getUsername());
        assertEquals(12L, response.getRecipient().getId());
        assertEquals("test12", response.getRecipient().getUsername());
    }
}

