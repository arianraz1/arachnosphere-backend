package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    void send_shouldReturnCreatedMessageResponse() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");
        MessageResponse expectedResponse = new MessageResponse();
        expectedResponse.setId(1L);
        expectedResponse.setContent("Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<MessageResponse> response = messageController.send(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void send_shouldThrowWhenSenderNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(1L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(1L, request)
        );
    }

    @Test
    void send_shouldThrowWhenRecipientNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(2L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(1L, request)
        );
    }

    @Test
    void send_shouldThrowWhenMessageIsInvalid() {
        SendMessageRequest request = new SendMessageRequest(1L, "Hello");

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself"));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageController.send(1L, request)
        );
    }

    @Test
    void inbox_shouldReturnPageOfMessages() {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setContent("Hello");
        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 20), 1);

        when(messageService.getInbox(eq(1L), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<MessageResponse>> response = messageController.inbox(1L, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(messageResponse);
    }

    @Test
    void inbox_shouldThrowWhenUserNotFound() {
        when(messageService.getInbox(eq(99L), any(PageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.inbox(99L, PageRequest.of(0, 20))
        );
    }

    @Test
    void sent_shouldReturnPageOfMessages() {
        MessageResponse messageResponse = new MessageResponse();
        messageResponse.setId(1L);
        messageResponse.setContent("Hello");
        Page<MessageResponse> page = new PageImpl<>(List.of(messageResponse), PageRequest.of(0, 20), 1);

        when(messageService.getSent(eq(1L), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<MessageResponse>> response = messageController.sent(1L, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(messageResponse);
    }

    @Test
    void sent_shouldThrowWhenUserNotFound() {
        when(messageService.getSent(eq(99L), any(PageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.sent(99L, PageRequest.of(0, 20))
        );
    }

    @Test
    void deleteFromInbox_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteFromInbox(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteFromInbox(1L, 1L);
    }

    @Test
    void deleteFromInbox_shouldThrowWhenMessageNotFound() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.MessageNotFoundException(99L))
                .when(messageService).deleteFromInbox(99L, 1L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageController.deleteFromInbox(99L, 1L)
        );
    }

    @Test
    void deleteFromInbox_shouldThrowWhenUnauthorized() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this message"))
                .when(messageService).deleteFromInbox(1L, 99L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteFromInbox(1L, 99L)
        );
    }

    @Test
    void deleteFromSent_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteFromSent(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteFromSent(1L, 1L);
    }

    @Test
    void deleteFromSent_shouldThrowWhenMessageNotFound() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.MessageNotFoundException(99L))
                .when(messageService).deleteFromSent(99L, 1L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageController.deleteFromSent(99L, 1L)
        );
    }

    @Test
    void deleteFromSent_shouldThrowWhenUnauthorized() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this message"))
                .when(messageService).deleteFromSent(1L, 99L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteFromSent(1L, 99L)
        );
    }
}
