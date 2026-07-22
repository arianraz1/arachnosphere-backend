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
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        List<MessageResponse> result = messageService.getThreadMessages(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(expectedResponse);
    }

    @Test
    void getThreadMessages_throwsWhenUserNotPartOfThread() {
        when(messageRepository.hasVisibleMessageInThread(1L, 99L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.getThreadMessages(1L, 99L));
    }

    @Test
    void getThreadMessages_returnsEmptyList_whenNoMessages() {
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        List<MessageResponse> result = messageService.getThreadMessages(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getThreadMessages_filtersSenderDeletedMessages() {
        message.setDeletedBySender(true);
        when(messageRepository.hasVisibleMessageInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));

        List<MessageResponse> result = messageService.getThreadMessages(1L, 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getThreadMessages_filtersRecipientDeletedMessages() {
        message.setDeletedByRecipient(true);
        when(messageRepository.hasVisibleMessageInThread(1L, 2L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));

        List<MessageResponse> result = messageService.getThreadMessages(1L, 2L);

        assertThat(result).isEmpty();
    }

    @Test
    void getThreadMessages_doesNotFilterMessageDeletedByOtherSide() {
        message.setDeletedBySender(true);
        MessageResponse expectedResponse = new MessageResponse();
        when(messageRepository.hasVisibleMessageInThread(1L, 2L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(message));
        when(messageMapper.toResponse(message)).thenReturn(expectedResponse);

        List<MessageResponse> result = messageService.getThreadMessages(1L, 2L);

        assertThat(result).hasSize(1);
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
    void deleteThreadForUser_softDeletesAllMySide_andDeletesFullyDeletedOnes() {
        // Prepare two messages in the same thread
        MessageEntity mine = new MessageEntity();
        mine.setId(10L);
        mine.setSender(sender); // I am sender
        mine.setRecipient(recipient);
        mine.setThread(thread);

        MessageEntity theirsButToMe = new MessageEntity();
        theirsButToMe.setId(11L);
        UserEntity other = new UserEntity();
        other.setId(3L);
        theirsButToMe.setSender(other); // other sent to me
        theirsButToMe.setRecipient(sender); // I am recipient
        theirsButToMe.setThread(thread);
        // Simulate that the other side already deleted this message on their side
        theirsButToMe.setDeletedBySender(true);

        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(mine, theirsButToMe));

        messageService.deleteThreadForUser(1L, 1L);

        // After operation: 'mine' should be soft-deleted by sender only -> saved, not deleted
        assertTrue(mine.isDeletedBySender());
        assertFalse(mine.isDeletedByRecipient());
        // 'theirsButToMe' should be deleted by both sides -> hard-deleted
        assertTrue(theirsButToMe.isDeletedByRecipient());

        ArgumentCaptor<List<MessageEntity>> saveAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageRepository).saveAll(saveAllCaptor.capture());
        assertThat(saveAllCaptor.getValue()).contains(mine).doesNotContain(theirsButToMe);

        ArgumentCaptor<List<MessageEntity>> deleteAllCaptor = ArgumentCaptor.forClass(List.class);
        verify(messageRepository).deleteAll(deleteAllCaptor.capture());
        assertThat(deleteAllCaptor.getValue()).contains(theirsButToMe);
    }

    @Test
    void deleteThreadForUser_throwsUnauthorized_whenNotParticipant() {
        when(messageRepository.isUserParticipantInThread(1L, 99L)).thenReturn(false);

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class,
                () -> messageService.deleteThreadForUser(1L, 99L));

        verify(messageRepository, never()).findByThreadIdOrderByCreatedAtAsc(anyLong());
        verify(messageRepository, never()).saveAll(any());
        verify(messageRepository, never()).deleteAll(any());
    }

    @Test
    void deleteThreadForUser_noOp_whenNoMessages() {
        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of());

        messageService.deleteThreadForUser(1L, 1L);

        verify(messageRepository, never()).saveAll(any());
        verify(messageRepository, never()).deleteAll(any());
    }

    @Test
    void deleteThreadForUser_deletesThread_whenNoMessagesRemain() {
        // Given two messages that will become fully deleted after my delete
        MessageEntity m1 = new MessageEntity();
        m1.setId(21L);
        m1.setSender(sender);
        m1.setRecipient(recipient);
        m1.setThread(thread);
        // Other side had already deleted
        m1.setDeletedByRecipient(true);

        MessageEntity m2 = new MessageEntity();
        m2.setId(22L);
        m2.setSender(new UserEntity());
        m2.getSender().setId(3L);
        m2.setRecipient(sender);
        m2.setThread(thread);
        // Other side had already deleted
        m2.setDeletedBySender(true);

        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(m1, m2));
        // After deletes, repository reports no messages remain for the thread
        when(messageRepository.existsByThreadId(1L)).thenReturn(false);

        messageService.deleteThreadForUser(1L, 1L);

        // Verify hard deletes were attempted for fully-deleted messages
        ArgumentCaptor<Iterable<MessageEntity>> delCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(messageRepository).deleteAll(delCaptor.capture());
        List<MessageEntity> deleted = (delCaptor.getValue() instanceof List)
                ? (List<MessageEntity>) delCaptor.getValue()
                : (delCaptor.getValue() != null)
                    ? (delCaptor.getValue() instanceof Iterable)
                        ? (java.util.stream.StreamSupport.stream(delCaptor.getValue().spliterator(), false).toList())
                        : List.of()
                    : List.of();
        assertThat(deleted).contains(m1, m2);
        // And thread cleanup performed
        verify(threadRepository).deleteById(1L);
    }

    @Test
    void deleteThreadForUser_doesNotDeleteThread_whenMessagesRemain() {
        MessageEntity m1 = new MessageEntity();
        m1.setId(31L);
        m1.setSender(sender);
        m1.setRecipient(recipient);
        m1.setThread(thread);

        when(messageRepository.isUserParticipantInThread(1L, 1L)).thenReturn(true);
        when(messageRepository.findByThreadIdOrderByCreatedAtAsc(1L)).thenReturn(List.of(m1));
        // Still at least one message remains afterwards
        when(messageRepository.existsByThreadId(1L)).thenReturn(true);

        messageService.deleteThreadForUser(1L, 1L);

        verify(threadRepository, never()).deleteById(anyLong());
    }
}