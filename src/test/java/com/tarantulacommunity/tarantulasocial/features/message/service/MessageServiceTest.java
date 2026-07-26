package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.CreateThreadRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.ReplyRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadStatusResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadParticipantEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.message.repository.MessageRepository;
import com.tarantulacommunity.tarantulasocial.features.message.repository.ThreadParticipantRepository;
import com.tarantulacommunity.tarantulasocial.features.message.repository.ThreadReadState;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    @Mock private MessageRepository messageRepository;
    @Mock private MessageMapper messageMapper;
    @Mock private UserRepository userRepository;
    @Mock private ThreadRepository threadRepository;
    @Mock private ThreadParticipantRepository threadParticipantRepository;

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

    // --- createThread ---

    @Test
    void createThread_createsThreadAndReturnsResponse() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "hello", "Subject");
        MessageResponse expectedResponse = new MessageResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        MessageResponse result = messageService.createThread(1L, request);

        assertEquals(expectedResponse, result);
        verify(threadRepository, atLeastOnce()).save(any(ThreadEntity.class));
    }

    @Test
    void createThread_usesProvidedSubject_whenSubjectGiven() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "hello", "Custom Subject");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.createThread(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).isEqualTo("Custom Subject");
    }

    @Test
    void createThread_usesContentAsSubject_whenSubjectNotProvided() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "hello", null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.createThread(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).isEqualTo("hello");
    }

    @Test
    void createThread_usesBlankSubjectFallback_whenSubjectIsBlank() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "hello", "   ");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.createThread(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).isEqualTo("hello");
    }

    @Test
    void createThread_truncatesSubjectFromContent_whenContentExceeds50Chars() {
        String longContent = "a".repeat(60);
        CreateThreadRequest request = new CreateThreadRequest(2L, longContent, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.createThread(1L, request);

        ArgumentCaptor<ThreadEntity> captor = ArgumentCaptor.forClass(ThreadEntity.class);
        verify(threadRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().getFirst().getSubject()).endsWith("...");
    }

    @Test
    void createThread_throwsUserNotFoundException_whenSenderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageService.createThread(1L, new CreateThreadRequest(2L, "hello", null)));
    }

    @Test
    void createThread_throwsUserNotFoundException_whenRecipientNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageService.createThread(1L, new CreateThreadRequest(2L, "hello", null)));
    }

    @Test
    void createThread_throwsInvalidMessageException_whenSendingToSelf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageService.createThread(1L, new CreateThreadRequest(1L, "hello", null)));
    }

    @Test
    void createThread_createsParticipantRowsForBothUsers() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "hello", "Subject");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageRepository.save(any())).thenReturn(message);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());

        messageService.createThread(1L, request);

        ArgumentCaptor<ThreadParticipantEntity> captor = ArgumentCaptor.forClass(ThreadParticipantEntity.class);
        verify(threadParticipantRepository, times(2)).save(captor.capture());

        List<ThreadParticipantEntity> saved = captor.getAllValues();
        assertThat(saved).anyMatch(p -> p.getUser().equals(sender) && p.getLastReadAt() != null);
        assertThat(saved).anyMatch(p -> p.getUser().equals(recipient) && p.getLastReadAt() == null);
    }

    // --- replyToThread ---

    @Test
    void replyToThread_returnsResponse_whenValid() {
        ReplyRequest request = new ReplyRequest("reply");
        MessageResponse expectedResponse = new MessageResponse();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadParticipantRepository.findOtherParticipant(1L, 1L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any())).thenReturn(message);
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        MessageResponse result = messageService.replyToThread(1L, 1L, request);

        assertEquals(expectedResponse, result);
    }

    @Test
    void replyToThread_throwsUserNotFoundException_whenSenderNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> messageService.replyToThread(1L, 1L, new ReplyRequest("reply")));
    }

    @Test
    void replyToThread_throwsWhenThreadNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageService.replyToThread(1L, 99L, new ReplyRequest("reply")));
    }

    @Test
    void replyToThread_throwsWhenThreadIsInactive() {
        thread.setActive(false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        assertThrows(GlobalExceptionHandler.InvalidMessageException.class,
                () -> messageService.replyToThread(1L, 1L, new ReplyRequest("reply")));
    }

    @Test
    void replyToThread_throwsWhenUserNotPartOfThread() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.replyToThread(1L, 1L, new ReplyRequest("reply")));

        verify(threadParticipantRepository, never()).findOtherParticipant(anyLong(), anyLong());
    }

    @Test
    void replyToThread_throwsUnauthorized_whenOtherParticipantMissing() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadParticipantRepository.findOtherParticipant(1L, 1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.replyToThread(1L, 1L, new ReplyRequest("reply")));

        verify(messageRepository, never()).save(any());
    }

    @Test
    void replyToThread_marksSenderReadAndEnsuresRecipientRow_whenRecipientRowAbsent() {
        ReplyRequest request = new ReplyRequest("reply");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadParticipantRepository.findOtherParticipant(1L, 1L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any())).thenReturn(message);
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());
        when(threadParticipantRepository.existsByThread_IdAndUser_Id(1L, 2L)).thenReturn(false);

        messageService.replyToThread(1L, 1L, request);

        verify(threadParticipantRepository).markRead(eq(1L), eq(1L), any(LocalDateTime.class));

        ArgumentCaptor<ThreadParticipantEntity> captor = ArgumentCaptor.forClass(ThreadParticipantEntity.class);
        verify(threadParticipantRepository).save(captor.capture());
        assertThat(captor.getValue().getUser()).isEqualTo(recipient);
        assertThat(captor.getValue().getLastReadAt()).isNull();
    }

    @Test
    void replyToThread_doesNotCreateRecipientRow_whenAlreadyExists() {
        ReplyRequest request = new ReplyRequest("reply");

        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadParticipantRepository.findOtherParticipant(1L, 1L)).thenReturn(Optional.of(recipient));
        when(messageRepository.save(any())).thenReturn(message);
        when(threadRepository.save(any())).thenReturn(thread);
        when(messageMapper.toResponse(any())).thenReturn(new MessageResponse());
        when(threadParticipantRepository.existsByThread_IdAndUser_Id(1L, 2L)).thenReturn(true);

        messageService.replyToThread(1L, 1L, request);

        verify(threadParticipantRepository, never()).save(any());
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

    // --- getThreadsForUser: unread computation ---

    @Test
    void getThreadsForUser_marksUnread_whenLatestMessageFromOtherUserAndNeverRead() {
        Page<ThreadEntity> threadPage = new PageImpl<>(List.of(thread));
        message.setSender(recipient);
        message.setRecipient(sender);
        message.setCreatedAt(LocalDateTime.now());

        when(messageRepository.findThreadsByUserId(1L, PageRequest.of(0, 20))).thenReturn(threadPage);
        when(messageRepository.findLatestVisibleMessagePerThread(any(), eq(1L))).thenReturn(List.of(message));
        when(threadParticipantRepository.findReadStatesForThreads(any(), eq(1L))).thenReturn(List.of());

        Page<ThreadSummaryResponse> result = messageService.getThreadsForUser(1L, PageRequest.of(0, 20));

        assertThat(result.getContent().getFirst().isUnread()).isTrue();
    }

    @Test
    void getThreadsForUser_marksRead_whenLastReadAfterLatestMessage() {
        Page<ThreadEntity> threadPage = new PageImpl<>(List.of(thread));
        LocalDateTime messageTime = LocalDateTime.now().minusMinutes(5);
        message.setSender(recipient);
        message.setRecipient(sender);
        message.setCreatedAt(messageTime);

        ThreadReadState readState = new ThreadReadState() {
            public Long getThreadId() { return 1L; }
            public LocalDateTime getLastReadAt() { return messageTime.plusMinutes(1); }
        };

        when(messageRepository.findThreadsByUserId(1L, PageRequest.of(0, 20))).thenReturn(threadPage);
        when(messageRepository.findLatestVisibleMessagePerThread(any(), eq(1L))).thenReturn(List.of(message));
        when(threadParticipantRepository.findReadStatesForThreads(any(), eq(1L))).thenReturn(List.of(readState));

        Page<ThreadSummaryResponse> result = messageService.getThreadsForUser(1L, PageRequest.of(0, 20));

        assertThat(result.getContent().getFirst().isUnread()).isFalse();
    }

    @Test
    void getThreadsForUser_marksRead_whenLatestMessageIsOwn() {
        Page<ThreadEntity> threadPage = new PageImpl<>(List.of(thread));
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setCreatedAt(LocalDateTime.now());

        when(messageRepository.findThreadsByUserId(1L, PageRequest.of(0, 20))).thenReturn(threadPage);
        when(messageRepository.findLatestVisibleMessagePerThread(any(), eq(1L))).thenReturn(List.of(message));
        when(threadParticipantRepository.findReadStatesForThreads(any(), eq(1L))).thenReturn(List.of());

        Page<ThreadSummaryResponse> result = messageService.getThreadsForUser(1L, PageRequest.of(0, 20));

        assertThat(result.getContent().getFirst().isUnread()).isFalse();
    }

    // --- getThreadMessages: markThreadReadIfNeeded ---

    @Test
    void getThreadMessages_marksRead_whenLatestMessageAfterLastRead() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(message)));
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());

        LocalDateTime latest = LocalDateTime.now();
        when(messageRepository.findLatestMessageTimestamp(1L, 1L)).thenReturn(latest);

        ThreadParticipantEntity participant = new ThreadParticipantEntity(thread, sender, latest.minusMinutes(5));
        when(threadParticipantRepository.findByThread_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(participant));
        when(threadParticipantRepository.markRead(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(1);

        messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 20));

        verify(threadParticipantRepository).markRead(eq(1L), eq(1L), any(LocalDateTime.class));
    }

    @Test
    void getThreadMessages_doesNotMarkRead_whenAlreadyReadPastLatest() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of(message)));
        when(messageMapper.toResponse(message)).thenReturn(new MessageResponse());

        LocalDateTime latest = LocalDateTime.now();
        when(messageRepository.findLatestMessageTimestamp(1L, 1L)).thenReturn(latest);

        ThreadParticipantEntity participant = new ThreadParticipantEntity(thread, sender, latest.plusMinutes(1));
        when(threadParticipantRepository.findByThread_IdAndUser_Id(1L, 1L)).thenReturn(Optional.of(participant));

        messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 20));

        verify(threadParticipantRepository, never()).markRead(anyLong(), anyLong(), any());
    }

    @Test
    void getThreadMessages_doesNotMarkRead_whenNoMessagesExist() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findVisibleMessagesByThreadId(1L, 1L, PageRequest.of(0, 20)))
                .thenReturn(Page.empty());
        when(messageRepository.findLatestMessageTimestamp(1L, 1L)).thenReturn(null);

        messageService.getThreadMessages(1L, 1L, PageRequest.of(0, 20));

        verify(threadParticipantRepository, never()).markRead(anyLong(), anyLong(), any());
    }

    // --- markThreadRead ---

    @Test
    void markThreadRead_updatesExistingParticipant() {
        when(threadParticipantRepository.markRead(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(1);

        messageService.markThreadRead(1L, 1L);

        verify(threadParticipantRepository, never()).save(any());
    }

    @Test
    void markThreadRead_createsParticipant_whenRowMissing() {
        when(threadParticipantRepository.markRead(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(0);
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));

        messageService.markThreadRead(1L, 1L);

        verify(threadParticipantRepository).save(any(ThreadParticipantEntity.class));
    }

    @Test
    void markThreadRead_throwsThreadNotFoundException_whenThreadMissing() {
        when(threadParticipantRepository.markRead(eq(1L), eq(1L), any(LocalDateTime.class))).thenReturn(0);
        when(threadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageService.markThreadRead(1L, 1L));
    }

    // --- hasUnreadMessages ---

    @Test
    void hasUnreadMessages_returnsTrue_whenRepositoryReturnsTrue() {
        when(threadParticipantRepository.existsUnreadForUser(1L)).thenReturn(true);

        assertTrue(messageService.hasUnreadMessages(1L));
    }

    @Test
    void hasUnreadMessages_returnsFalse_whenRepositoryReturnsFalse() {
        when(threadParticipantRepository.existsUnreadForUser(1L)).thenReturn(false);

        assertFalse(messageService.hasUnreadMessages(1L));
    }

    // --- getThreadStatus ---

    @Test
    void getThreadStatus_returnsActiveTrue_whenThreadIsActive() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        ThreadStatusResponse result = messageService.getThreadStatus(1L, 1L);

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void getThreadStatus_returnsActiveFalse_whenThreadIsInactive() {
        thread.setActive(false);
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadRepository.findById(1L)).thenReturn(Optional.of(thread));

        ThreadStatusResponse result = messageService.getThreadStatus(1L, 1L);

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void getThreadStatus_throwsUnauthorized_whenUserNotPartOfThread() {
        when(messageRepository.hasVisibleMessageInThread(1L, 99L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.getThreadStatus(1L, 99L));

        verify(threadRepository, never()).findById(anyLong());
    }

    @Test
    void getThreadStatus_throwsThreadNotFoundException_whenThreadMissing() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(threadRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.ThreadNotFoundException.class,
                () -> messageService.getThreadStatus(1L, 1L));
    }
}