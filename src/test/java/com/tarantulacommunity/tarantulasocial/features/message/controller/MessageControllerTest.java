package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.CreateThreadRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.ReplyRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadStatusResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.UnreadStatusResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
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

    // --- createThread ---

    @Test
    void createThread_shouldReturnCreatedMessageResponse() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello", null);
        MessageResponse expectedResponse = new MessageResponse();
        expectedResponse.setId(1L);
        expectedResponse.setContent("Hello");

        when(messageService.createThread(eq(1L), any(CreateThreadRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<MessageResponse> response = messageController.createThread(userDetailsOf(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void createThread_shouldThrowWhenSenderNotFound() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello", null);

        when(messageService.createThread(eq(1L), any(CreateThreadRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(1L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.createThread(userDetailsOf(1L), request));
    }

    @Test
    void createThread_shouldThrowWhenRecipientNotFound() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello", null);

        when(messageService.createThread(eq(1L), any(CreateThreadRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(2L));

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageController.createThread(userDetailsOf(1L), request));
    }

    @Test
    void createThread_shouldThrowWhenSendingToSelf() {
        CreateThreadRequest request = new CreateThreadRequest(1L, "Hello", null);

        when(messageService.createThread(eq(1L), any(CreateThreadRequest.class)))
                .thenThrow(new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself"));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageController.createThread(userDetailsOf(1L), request));
    }

    // --- reply ---

    @Test
    void reply_shouldReturnCreatedMessageResponse() {
        ReplyRequest request = new ReplyRequest("Hello");
        MessageResponse expectedResponse = new MessageResponse();
        expectedResponse.setId(1L);
        expectedResponse.setContent("Hello");

        when(messageService.replyToThread(eq(1L), eq(1L), any(ReplyRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<MessageResponse> response = messageController.reply(1L, userDetailsOf(1L), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void reply_shouldThrowWhenThreadNotFound() {
        ReplyRequest request = new ReplyRequest("Hello");

        when(messageService.replyToThread(eq(1L), eq(99L), any(ReplyRequest.class)))
                .thenThrow(new GlobalExceptionHandler.ThreadNotFoundException(99L));

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageController.reply(99L, userDetailsOf(1L), request));
    }

    @Test
    void reply_shouldThrowWhenNotPartOfThread() {
        ReplyRequest request = new ReplyRequest("Hello");

        when(messageService.replyToThread(eq(99L), eq(1L), any(ReplyRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.reply(1L, userDetailsOf(99L), request));
    }

    @Test
    void reply_shouldThrowWhenThreadInactive() {
        ReplyRequest request = new ReplyRequest("Hello");

        when(messageService.replyToThread(eq(1L), eq(1L), any(ReplyRequest.class)))
                .thenThrow(new GlobalExceptionHandler.InvalidMessageException("This thread is no longer active"));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageController.reply(1L, userDetailsOf(1L), request));
    }

    // --- inbox ---

    @Test
    void inbox_shouldReturnPageOfThreadSummaries() {
        ThreadSummaryResponse summary = new ThreadSummaryResponse(1L, "Subject", "user2", 2L, "Hello...", LocalDateTime.now(), true, false);
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

    // --- getThread ---

    @Test
    void getThread_shouldReturnMessagesInThread() {
        MessageResponse msg = new MessageResponse();
        msg.setId(1L);
        msg.setContent("Hello");
        Page<MessageResponse> page = new PageImpl<>(List.of(msg), PageRequest.of(0, 20), 1);

        when(messageService.getThreadMessages(eq(1L), eq(1L), any(Pageable.class))).thenReturn(page);

        ResponseEntity<Page<MessageResponse>> response =
                messageController.getThread(1L, userDetailsOf(1L), PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(msg);
    }

    @Test
    void getThread_shouldThrowWhenNotPartOfThread() {
        when(messageService.getThreadMessages(eq(1L), eq(99L), any(Pageable.class)))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.getThread(1L, userDetailsOf(99L), PageRequest.of(0, 20)));
    }

    // --- deleteMessage ---

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

    // --- deleteThreadForMe ---

    @Test
    void deleteThreadForMe_shouldReturnNoContent() {
        ResponseEntity<Void> response = messageController.deleteThreadForMe(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(messageService).deleteThreadForUser(1L, 1L);
    }

    @Test
    void deleteThreadForMe_shouldThrowWhenUnauthorized() {
        doThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"))
                .when(messageService).deleteThreadForUser(1L, 99L);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.deleteThreadForMe(1L, userDetailsOf(99L)));
    }

    // --- hasUnread ---

    @Test
    void hasUnread_returnsTrue_whenUserHasUnreadMessages() {
        when(messageService.hasUnreadMessages(1L)).thenReturn(true);

        ResponseEntity<UnreadStatusResponse> response = messageController.hasUnread(userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().unread()).isTrue();
    }

    @Test
    void hasUnread_returnsFalse_whenUserHasNoUnreadMessages() {
        when(messageService.hasUnreadMessages(1L)).thenReturn(false);

        ResponseEntity<UnreadStatusResponse> response = messageController.hasUnread(userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().unread()).isFalse();
    }

    // --- getThreadStatus ---

    @Test
    void getThreadStatus_shouldReturnActiveStatus() {
        ThreadStatusResponse expectedResponse = new ThreadStatusResponse(true);

        when(messageService.getThreadStatus(1L, 1L)).thenReturn(expectedResponse);

        ResponseEntity<ThreadStatusResponse> response =
                messageController.getThreadStatus(1L, userDetailsOf(1L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().isActive()).isTrue();
    }

    @Test
    void getThreadStatus_shouldThrowWhenNotPartOfThread() {
        when(messageService.getThreadStatus(1L, 99L))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation"));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageController.getThreadStatus(1L, userDetailsOf(99L)));
    }

    @Test
    void getThreadStatus_shouldThrowWhenThreadNotFound() {
        when(messageService.getThreadStatus(99L, 1L))
                .thenThrow(new GlobalExceptionHandler.ThreadNotFoundException(99L));

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageController.getThreadStatus(99L, userDetailsOf(1L)));
    }
}