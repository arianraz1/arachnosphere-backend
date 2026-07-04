package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.config.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
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
        SendMessageRequest request = new SendMessageRequest(2L, "Hello", null, null);
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
        SendMessageRequest request = new SendMessageRequest(2L, "Hello", null, null);

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(1L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenRecipientNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello", null, null);

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(2L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenSendingToSelf() {
        SendMessageRequest request = new SendMessageRequest(1L, "Hello", null, null);

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself"));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenThreadNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello", null, 99L);

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.ThreadNotFoundException(99L));

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void send_shouldThrowWhenNotPartOfThread() {
        SendMessageRequest request = new SendMessageRequest(2L, "Hello", null, 1L);

        when(messageService.sendMessage(eq(1L), any(SendMessageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.send(userDetailsOf(1L), request));
    }

    @Test
    void inbox_shouldReturnPageOfThreadSummaries() {
        ThreadSummaryResponse summary = new ThreadSummaryResponse(1L, "Subject", "user2", 2L, "Hello...", LocalDateTime.now(), true);
        Page<ThreadSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);

        when(messageService.getThreadsForUser(eq(1L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<ThreadSummaryResponse>> response =
                messageController.inbox(userDetailsOf(1L), PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(summary);
    }

    @Test
    void inbox_shouldReturnEmptyPageWhenNoThreads() {
        when(messageService.getThreadsForUser(eq(1L), any(Pageable.class))).thenReturn(Page.empty());

        ResponseEntity<Page<ThreadSummaryResponse>> response =
                messageController.inbox(userDetailsOf(1L), PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getThread_shouldReturnMessagesInThread() {
        MessageResponse msg = new MessageResponse();
        msg.setId(1L);
        msg.setContent("Hello");

        when(messageService.getThreadMessages(eq(1L), eq(1L))).thenReturn(List.of(msg));

        ResponseEntity<List<MessageResponse>> response =
                messageController.getThread(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst()).isEqualTo(msg);
    }

    @Test
    void getThread_shouldThrowWhenNotPartOfThread() {
        when(messageService.getThreadMessages(eq(1L), eq(99L)))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.getThread(1L, userDetailsOf(99L)));
    }

    @Test
    void deleteMessage_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteMessage(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteMessage(1L, 1L);
    }

    @Test
    void deleteMessage_shouldThrowWhenMessageNotFound() {
        doThrow(new GlobalExceptionHandler.MessageNotFoundException(99L))
                .when(messageService).deleteMessage(99L, 1L);

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageController.deleteMessage(99L, userDetailsOf(1L)));
    }

    @Test
    void deleteMessage_shouldThrowWhenUnauthorized() {
        doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this message"))
                .when(messageService).deleteMessage(1L, 99L);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteMessage(1L, userDetailsOf(99L)));
    }
}