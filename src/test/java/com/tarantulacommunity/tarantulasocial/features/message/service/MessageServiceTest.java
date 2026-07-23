package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.message.repository.MessageRepository;
import com.tarantulacommunity.tarantulasocial.features.message.repository.ThreadRepository;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private UserRepository userRepository;
    @Mock private ThreadRepository threadRepository;

    @InjectMocks
    private MessageService messageService;

    private UserEntity sender;
    private UserEntity recipient;
    private MessageEntity message;
    private ThreadEntity thread;

    @BeforeEach
    void setUp() {
        sender = new UserEntity();
        sender.setId(1L);

        recipient = new UserEntity();
        recipient.setId(2L);

        thread = new ThreadEntity();
        thread.setId(1L);
        thread.setCreatedBy(sender);
        thread.setSubject("Test subject");
        thread.setActive(true);

        message = new MessageEntity();
        message.setId(1L);
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent("hello");
        message.setThread(thread);
    }

    // --- sendMessage ---

    @Test
    void sendMessage_createsNewThread_whenNoThreadIdProvided() {
        SendMessageRequest request = new SendMessageRequest(2L, "hello", "Subject", null);
        MessageResponse expectedResponse = new MessageResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        MessageResponse result = messageService.sendMessage(1L, request);

        assertEquals(expectedResponse, result);
        verify(threadRepository, atLeastOnce()).save(any(ThreadEntity.class));
    }

    @Test
    void sendMessage_usesContentAsSubject_whenSubjectNotProvided() {
        SendMessageRequest request = new SendMessageRequest(2L, "hello", null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.sendMessage(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).isEqualTo("hello");
    }

    @Test
    void sendMessage_truncatesSubjectFromContent_whenContentExceeds50Chars() {
        String longContent = "a".repeat(60);
        SendMessageRequest request = new SendMessageRequest(2L, longContent, null, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.sendMessage(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).endsWith("...");
    }

    @Test
    void sendMessage_repliesOnExistingThread_whenThreadIdProvided() {
        SendMessageRequest request = new SendMessageRequest(2L, "reply", null, 1L);
        MessageResponse expectedResponse = new MessageResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.save(any())).thenReturn(message);
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        MessageResponse result = messageService.sendMessage(1L, request);

        assertEquals(expectedResponse, result);
    }

    @Test
    void sendMessage_throwsWhenThreadNotFound() {
        SendMessageRequest request = new SendMessageRequest(2L, "reply", null, 99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageService.sendMessage(1L, request));
    }

    @Test
    void sendMessage_throwsWhenThreadIsInactive() {
        thread.setActive(false);
        SendMessageRequest request = new SendMessageRequest(2L, "reply", null, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageService.sendMessage(1L, request));
    }

    @Test
    void sendMessage_throwsWhenUserNotPartOfThread() {
        SendMessageRequest request = new SendMessageRequest(2L, "reply", null, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.sendMessage(1L, request));
    }

    @Test
    void sendMessage_throwsUserNotFoundException_whenSenderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageService.sendMessage(1L, new SendMessageRequest(2L, "hello", null, null)));
    }

    @Test
    void sendMessage_throwsUserNotFoundException_whenRecipientNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageService.sendMessage(1L, new SendMessageRequest(2L, "hello", null, null)));
    }

    @Test
    void sendMessage_throwsInvalidMessageException_whenSendingToSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageService.sendMessage(1L, new SendMessageRequest(1L, "hello", null, null)));
    }

    // --- getThreadsForUser ---

    @Test
    void getThreadsForUser_returnsPageOfThreadSummaries() {
        Page<ThreadEntity> threadPage = new PageImpl<>(List.of(thread));
        when(messageRepository.findThreadsByUserId(1L, PageRequest.of(0, 20))).thenReturn(threadPage);
        when(messageRepository.findLatestVisibleMessagePerThread(any(), eq(1L))).thenReturn(List.of(message));

        message.setSender(sender);
        message.setRecipient(recipient);
        message.setCreatedAt(LocalDateTime.now());
        message.setThread(thread);
        recipient.setUsername("user2");

        Page<ThreadSummaryResponse> result = messageService.getThreadsForUser(1L, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getThreadId()).isEqualTo(1L);
        assertThat(result.getContent().getFirst().getOtherParticipantUsername()).isEqualTo("user2");
    }

    @Test
    void getThreadsForUser_returnsEmptyPage_whenNoThreads() {
        when(messageRepository.findThreadsByUserId(1L, PageRequest.of(0, 20))).thenReturn(Page.empty());
        when(messageRepository.findLatestVisibleMessagePerThread(any(), eq(1L))).thenReturn(List.of());

        Page<ThreadSummaryResponse> result = messageService.getThreadsForUser(1L, PageRequest.of(0, 20));

        assertThat(result.isEmpty()).isTrue();
    }

    // --- getThreadMessages ---

    @Test
    void getThreadMessages_returnsMessages_whenUserIsPartOfThread() {
        MessageResponse expectedResponse = new MessageResponse();
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(message)));
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        Page<MessageResponse> result = messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 20));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst()).isEqualTo(expectedResponse);
    }

    @Test
    void getThreadMessages_throwsWhenUserNotPartOfThread() {
        when(messageRepository.hasVisibleMessageInThread(1L, 99L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.getThreadMessages(1L, 99L, PageRequest.of(0, 20)));
    }

    @Test
    void getThreadMessages_returnsEmptyPage_whenNoMessages() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 20)))
                .thenReturn(Page.empty());

        Page<MessageResponse> result = messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 20));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void getThreadMessages_respectsPagination() {
        MessageEntity message1 = new MessageEntity();
        message1.setId(1L);
        message1.setSender(sender);
        message1.setRecipient(recipient);

        MessageEntity message2 = new MessageEntity();
        message2.setId(2L);
        message2.setSender(sender);
        message2.setRecipient(recipient);

        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 2)))
                .thenReturn(new PageImpl<>(List.of(message1, message2)));
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        Page<MessageResponse> result = messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 2));

        assertThat(result.getContent()).hasSize(2);
    }

    // --- deleteMessage ---

    @Test
    void deleteMessage_setDeletedBySender_whenUserIsSender() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(1L, 1L);

        assertTrue(message.isDeletedBySender());
        verify(messageRepository).save(message);
    }

    @Test
    void deleteMessage_setDeletedByRecipient_whenUserIsRecipient() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(1L, 2L);

        assertTrue(message.isDeletedByRecipient());
        verify(messageRepository).save(message);
    }

    @Test
    void deleteMessage_hardDeletes_whenBothFlagsSet() {
        message.setDeletedBySender(true);
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        messageService.deleteMessage(1L, 2L);

        verify(messageRepository).delete(message);
        verify(messageRepository, never()).save(any());
    }

    @Test
    void deleteMessage_throwsMessageNotFoundException_whenNotFound() {
        when(messageRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.MessageNotFoundException.class,
                () -> messageService.deleteMessage(99L, 1L));
    }

    @Test
    void deleteMessage_throwsUnauthorizedException_whenUserNotInvolved() {
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.deleteMessage(1L, 99L));
    }

    // --- deleteThreadForUser ---

    @Test
    void deleteThreadForUser_callsBulkQueries() {
        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.markDeletedBySenderForThread(1L, 1L)).thenReturn(1);
        when(messageRepository.markDeletedByRecipientForThread(1L, 1L)).thenReturn(1);
        when(messageRepository.deleteFullyDeletedMessagesInThread(1L)).thenReturn(0);
        when(messageRepository.existsByThreadId(1L)).thenReturn(true);

        messageService.deleteThreadForUser(1L, 1L);

        verify(messageRepository).markDeletedBySenderForThread(1L, 1L);
        verify(messageRepository).markDeletedByRecipientForThread(1L, 1L);
        verify(messageRepository).deleteFullyDeletedMessagesInThread(1L);
    }

    @Test
    void deleteThreadForUser_throwsUnauthorized_whenNotParticipant() {
        when(messageRepository.isUserParticipantInThread(1L, 99L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.deleteThreadForUser(1L, 99L));

        verify(messageRepository, never()).markDeletedBySenderForThread(anyLong(), anyLong());
        verify(messageRepository, never()).markDeletedByRecipientForThread(anyLong(), anyLong());
        verify(messageRepository, never()).deleteFullyDeletedMessagesInThread(anyLong());
    }

    @Test
    void deleteThreadForUser_callsQueriesEvenWhenNoMessages() {
        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.markDeletedBySenderForThread(1L, 1L)).thenReturn(0);
        when(messageRepository.markDeletedByRecipientForThread(1L, 1L)).thenReturn(0);
        when(messageRepository.deleteFullyDeletedMessagesInThread(1L)).thenReturn(0);
        when(messageRepository.existsByThreadId(1L)).thenReturn(false);

        messageService.deleteThreadForUser(1L, 1L);

        verify(messageRepository).markDeletedBySenderForThread(1L, 1L);
        verify(messageRepository).markDeletedByRecipientForThread(1L, 1L);
        verify(messageRepository).deleteFullyDeletedMessagesInThread(1L);
        verify(threadRepository).deleteById(1L);
    }

    @Test
    void deleteThreadForUser_deletesThread_whenNoMessagesRemain() {
        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.markDeletedBySenderForThread(1L, 1L)).thenReturn(2);
        when(messageRepository.markDeletedByRecipientForThread(1L, 1L)).thenReturn(2);
        when(messageRepository.deleteFullyDeletedMessagesInThread(1L)).thenReturn(2);
        when(messageRepository.existsByThreadId(1L)).thenReturn(false);

        messageService.deleteThreadForUser(1L, 1L);

        verify(messageRepository).deleteFullyDeletedMessagesInThread(1L);
        verify(threadRepository).deleteById(1L);
    }

    @Test
    void deleteThreadForUser_doesNotDeleteThread_whenMessagesRemain() {
        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.markDeletedBySenderForThread(1L, 1L)).thenReturn(1);
        when(messageRepository.markDeletedByRecipientForThread(1L, 1L)).thenReturn(0);
        when(messageRepository.deleteFullyDeletedMessagesInThread(1L)).thenReturn(0);
        when(messageRepository.existsByThreadId(1L)).thenReturn(true);

        messageService.deleteThreadForUser(1L, 1L);

        verify(threadRepository, never()).deleteById(anyLong());
    }
}