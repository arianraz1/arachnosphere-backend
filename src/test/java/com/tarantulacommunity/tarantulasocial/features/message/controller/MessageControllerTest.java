package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    private CustomUserDetails userDetailsOf(Long id) {
        UserEntity user = new UserEntity();
        user.setId(id);
        return new CustomUserDetails(user, null);
    }

    @Test
    void send_shouldReturnCreatedMessageResponse() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");
        MessageResponse expectedResponse = new MessageResponse();
        expectedResponse.setId(1L);
        expectedResponse.setContent("Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<MessageResponse> response = messageController.send(userDetailsOf(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void send_shouldThrowWhenSenderNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(1L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenRecipientNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(2L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenMessageIsInvalid() {
        SendMessageRequest request = new SendMessageRequest(1L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself"));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void inbox_shouldReturnPageOfMessages() {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setContent("Hello");
        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 20), 1);

        when(messageService.getInbox(eq(1L), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<MessageResponse>> response =
                messageController.inbox(userDetailsOf(1L), PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(messageResponse);
    }

    @Test
    void inbox_shouldThrowWhenUserNotFound() {
        when(messageService.getInbox(eq(99L), any(PageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.inbox(userDetailsOf(99L), PageRequest.of(0, 20)));
    }

    @Test
    void sent_shouldReturnPageOfMessages() {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setContent("Hello");
        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 20), 1);

        when(messageService.getSent(eq(1L), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<MessageResponse>> response =
                messageController.sent(userDetailsOf(1L), PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(messageResponse);
    }

    @Test
    void sent_shouldThrowWhenUserNotFound() {
        when(messageService.getSent(eq(99L), any(PageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.sent(userDetailsOf(99L), PageRequest.of(0, 20)));
    }

    @Test
    void deleteFromInbox_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteFromInbox(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteFromInbox(1L, 1L);
    }

    @Test
    void deleteFromInbox_shouldThrowWhenMessageNotFound() {
        doThrow(new GlobalExceptionHandler.MessageNotFoundException(99L))
                .when(messageService).deleteFromInbox(99L, 1L);

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageController.deleteFromInbox(99L, userDetailsOf(1L)));
    }

    @Test
    void deleteFromInbox_shouldThrowWhenUnauthorized() {
        doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this message"))
                .when(messageService).deleteFromInbox(1L, 99L);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteFromInbox(1L, userDetailsOf(99L)));
    }

    @Test
    void deleteFromSent_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteFromSent(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteFromSent(1L, 1L);
    }

    @Test
    void deleteFromSent_shouldThrowWhenMessageNotFound() {
        doThrow(new GlobalExceptionHandler.MessageNotFoundException(99L))
                .when(messageService).deleteFromSent(99L, 1L);

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageController.deleteFromSent(99L, userDetailsOf(1L)));
    }

    @Test
    void deleteFromSent_shouldThrowWhenUnauthorized() {
        doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this message"))
                .when(messageService).deleteFromSent(1L, 99L);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteFromSent(1L, userDetailsOf(99L)));
    }
}