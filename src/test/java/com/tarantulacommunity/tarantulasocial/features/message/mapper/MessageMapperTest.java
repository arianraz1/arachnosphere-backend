package com.tarantulacommunity.tarantulasocial.features.message.mapper;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    private ThreadEntity thread;

    @BeforeEach
    void setUp() {
        UserEntity creator = TestUtilities.verifiedUser("0", "0");
        creator.setId(0L);
        thread = new ThreadEntity();
        thread.setId(1L);
        thread.setCreatedBy(creator);
        thread.setSubject("Test thread");
    }

    @Test
    void testToResponse() {
        UserEntity sender = TestUtilities.verifiedUser("1", "1");
        sender.setId(1L);
        sender.setProfilePicUrl("https://example.com/sender.jpg");

        UserEntity recipient = TestUtilities.verifiedUser("2", "2");
        recipient.setId(2L);
        recipient.setProfilePicUrl("https://example.com/recipient.jpg");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hello!", thread);
        message.setId(1L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertEquals(message.getId(), response.getId());
        assertEquals(message.getContent(), response.getContent());
        assertEquals(message.getCreatedAt(), response.getCreatedAt());
        assertEquals(thread.getId(), response.getThreadId());
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
        UserEntity sender = TestUtilities.verifiedUser("3", "3");
        sender.setId(3L);

        UserEntity recipient = TestUtilities.verifiedUser("4", "4");
        recipient.setId(4L);

        String longContent = "This is a very long message about tarantulas. " +
                "I have many species in my collection including the Brazilian Black, " +
                "Singapore Blue, and Chilean Rose Hair. They are amazing!";

        MessageEntity message = TestUtilities.validMessage(sender, recipient, longContent, thread);
        message.setId(2L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertEquals(longContent, response.getContent());
        assertEquals(message.getId(), response.getId());
    }

    @Test
    void testToResponsePreservesTimestamp() {
        UserEntity sender = TestUtilities.verifiedUser("5", "5");
        sender.setId(5L);

        UserEntity recipient = TestUtilities.verifiedUser("6", "6");
        recipient.setId(6L);

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Test", thread);
        message.setId(3L);
        LocalDateTime timestamp = LocalDateTime.of(2025, 6, 15, 14, 30, 0);
        message.setCreatedAt(timestamp);

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals(timestamp, response.getCreatedAt());
    }

    @Test
    void testToResponseWithDifferentUsers() {
        UserEntity sender = TestUtilities.verifiedUser("7", "7");
        sender.setId(7L);
        sender.setProfilePicUrl("https://example.com/sender7.jpg");

        UserEntity recipient = TestUtilities.verifiedUser("8", "8");
        recipient.setId(8L);
        recipient.setProfilePicUrl("https://example.com/recipient8.jpg");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Another message", thread);
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
        UserEntity sender = TestUtilities.verifiedUser("9", "9");
        sender.setId(9L);

        UserEntity recipient = TestUtilities.verifiedUser("10", "10");
        recipient.setId(10L);

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Message without pics", thread);
        message.setId(5L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertNotNull(response);
        assertNull(response.getSender().getProfilePicUrl());
        assertNull(response.getRecipient().getProfilePicUrl());
    }

    @Test
    void testToResponseMapsAllFields() {
        UserEntity sender = TestUtilities.verifiedUser("11", "11");
        sender.setId(11L);
        sender.setProfilePicUrl("https://example.com/sender11.jpg");

        UserEntity recipient = TestUtilities.verifiedUser("12", "12");
        recipient.setId(12L);
        recipient.setProfilePicUrl("https://example.com/recipient12.jpg");

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Complete message test", thread);
        message.setId(6L);
        LocalDateTime createdAt = LocalDateTime.of(2025, 12, 25, 12, 0, 0);
        message.setCreatedAt(createdAt);

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals(6L, response.getId());
        assertEquals("Complete message test", response.getContent());
        assertEquals(createdAt, response.getCreatedAt());
        assertEquals(thread.getId(), response.getThreadId());
        assertEquals(11L, response.getSender().getId());
        assertEquals("test11", response.getSender().getUsername());
        assertEquals(12L, response.getRecipient().getId());
        assertEquals("test12", response.getRecipient().getUsername());
    }

    @Test
    void testToResponseMapsThreadId() {
        UserEntity sender = TestUtilities.verifiedUser("13", "13");
        sender.setId(13L);

        UserEntity recipient = TestUtilities.verifiedUser("14", "14");
        recipient.setId(14L);

        ThreadEntity specificThread = new ThreadEntity();
        specificThread.setId(5L);
        specificThread.setCreatedBy(sender);

        MessageEntity message = TestUtilities.validMessage(sender, recipient, "Hello", specificThread);
        message.setId(7L);
        message.setCreatedAt(LocalDateTime.now());

        MessageResponse response = messageMapper.toResponse(message);

        assertEquals(5L, response.getThreadId());
    }
}