package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.message.repository.MessageRepository;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.features.user.dto.response.UserSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MessageMapper messageMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MessageService messageService;

    private UserEntity sender;
    private UserEntity recipient;
    private MessageEntity message;
    private UserSummaryResponse senderSummary;
    private UserSummaryResponse recipientSummary;

    @BeforeEach
    void setUp() {
        sender = new UserEntity();
        sender.setId(1L);

        recipient = new UserEntity();
        recipient.setId(2L);

        message = new MessageEntity();
        message.setId(1L);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("hello");

        senderSummary = new UserSummaryResponse();
        senderSummary.setId(1L);

        recipientSummary = new UserSummaryResponse();
        recipientSummary.setId(2L);
    }

    @Test
    void sendMessage_shouldReturnMessageResponse_whenValid() {
        MessageResponse expectedResponse = new MessageResponse();

        expectedResponse.setId(1L);
        expectedResponse.setSender(senderSummary);
        expectedResponse.setRecipient(recipientSummary);
        expectedResponse.setContent("hello");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));

        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);
        when(messageRepository.save(any(MessageEntity.class))).thenReturn(message);

        MessageResponse actualResponse = messageService.sendMessage(1L, new SendMessageRequest(2L, "hello"));

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void sendMessage_shouldThrowUserNotFoundException_whenSenderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                messageService.sendMessage(1L, new SendMessageRequest(2L, "hello")));
    }

    @Test
    void sendMessage_shouldThrowUserNotFoundException_whenRecipientNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                messageService.sendMessage(1L, new SendMessageRequest(2L, "hello")));
    }

    @Test
    void sendMessage_shouldThrowInvalidMessageException_whenSenderAndRecipientAreSameUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class, () ->
                messageService.sendMessage(1L, new SendMessageRequest(1L, "hello")));
    }

    @Test
    void getInbox_shouldReturnPagedMessages_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setSender(senderSummary);
        messageResponse.setRecipient(recipientSummary);
        messageResponse.setContent("hello");

        Page<MessageEntity> entityPage = new PageImpl<>(List.of(message), PageRequest.of(0, 10), 1);

        when(messageRepository.findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(
                eq(sender), any(Pageable.class))).thenReturn(entityPage);
        when(messageMapper.toResponse(message)).thenReturn(messageResponse);

        Page<MessageResponse> actualResponse = messageService.getInbox(1L, PageRequest.of(0, 10));

        assertEquals(1, actualResponse.getTotalElements());
        assertEquals(messageResponse, actualResponse.getContent().getFirst());
    }

    @Test
    void getInbox_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                messageService.getInbox(1L, PageRequest.of(0, 10)));
    }

    @Test
    void getSentMessages_shouldReturnPagedMessages_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setSender(senderSummary);
        messageResponse.setRecipient(recipientSummary);
        messageResponse.setContent("hello");

        Page<MessageEntity> entityPage = new PageImpl<>(List.of(message), PageRequest.of(0, 10), 1);

        when(messageRepository.findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(
                eq(sender), any(Pageable.class))).thenReturn(entityPage);
        when(messageMapper.toResponse(message)).thenReturn(messageResponse);

        Page<MessageResponse> actualResponse = messageService.getSent(1L, PageRequest.of(0, 10));

        assertEquals(1, actualResponse.getTotalElements());
        assertEquals(messageResponse, actualResponse.getContent().getFirst());
    }

    @Test
    void getSent_shouldThrowUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                messageService.getSent(1L, PageRequest.of(0, 10)));
    }

    @Test
    void deleteFromInbox_shouldSetDeletedByRecipientTrue_whenUserIsRecipient() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteFromInbox(1L, 2L);

        assertTrue(message.isDeletedByRecipient());
    }

    @Test
    void deleteFromInbox_shouldThrowMessageNotFoundException_whenMessageNotFound() {
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class, () ->
                messageService.deleteFromInbox(1L, 2L));
    }

    @Test
    void deleteFromInbox_shouldThrowUnauthorizedException_whenUserIsNotRecipient() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class, () ->
                messageService.deleteFromInbox(1L, 99L));
    }

    @Test
    void deleteFromSent_shouldSetDeletedBySenderTrue_whenUserIsSender() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteFromSent(1L, 1L);

        assertTrue(message.isDeletedBySender());
    }

    @Test
    void deleteFromSent_shouldThrowMessageNotFoundException_whenMessageNotFound() {
        when(messageRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class, () ->
                messageService.deleteFromSent(1L, 1L));
    }

    @Test
    void deleteFromSent_shouldThrowUnauthorizedException_whenUserIsNotSender() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class, () ->
                messageService.deleteFromSent(1L, 99L));
    }
}
