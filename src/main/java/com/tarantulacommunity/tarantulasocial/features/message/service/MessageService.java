package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final ThreadRepository threadRepository;
    private final ThreadParticipantRepository threadParticipantRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(senderId));
        UserEntity recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(request.getRecipientId()));

        if (sender.getId().equals(recipient.getId())) {
            throw new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself");
        }

        ThreadEntity thread;
        boolean newThread = request.getThreadId() == null;

        if (!newThread) {
            thread = threadRepository.findById(request.getThreadId())
                    .orElseThrow(() -> new GlobalExceptionHandler.ThreadNotFoundException(request.getThreadId()));

            if (!thread.isActive()) {
                throw new GlobalExceptionHandler.InvalidMessageException("This thread is no longer active");
            }

            if (!messageRepository.hasVisibleMessageInThread(thread.getId(), senderId)) {
                throw new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation");
            }
        } else {
            thread = new ThreadEntity();
            thread.setCreatedBy(sender);
            if (request.getSubject() != null && !request.getSubject().isBlank()) {
                thread.setSubject(request.getSubject());
            } else {
                String preview = request.getContent().length() > 50
                        ? request.getContent().substring(0, 50) + "..."
                        : request.getContent();
                thread.setSubject(preview);
            }
            thread = threadRepository.save(thread);
        }

        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.getContent());
        message.setThread(thread);

        MessageEntity saved = messageRepository.save(message);

        thread.setUpdatedAt(LocalDateTime.now());
        threadRepository.save(thread);

        // Sender has implicitly "read" up to the message they just sent.
        if (newThread) {
            threadParticipantRepository.save(new ThreadParticipantEntity(thread, sender, LocalDateTime.now()));
            threadParticipantRepository.save(new ThreadParticipantEntity(thread, recipient, null));
        } else {
            threadParticipantRepository.markRead(thread.getId(), senderId, LocalDateTime.now());
            // Defensive: recipient may not have a row yet (e.g. thread predates this feature
            // and backfill somehow missed them). Ensure it exists without touching last_read_at.
            if (!threadParticipantRepository.existsByThread_IdAndUser_Id(thread.getId(), recipient.getId())) {
                threadParticipantRepository.save(new ThreadParticipantEntity(thread, recipient, null));
            }
        }

        return messageMapper.toResponse(saved);
    }

    public Page<ThreadSummaryResponse> getThreadsForUser(Long userId, Pageable pageable) {
        Pageable unsortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());

        Page<ThreadEntity> threads = messageRepository.findThreadsByUserId(userId, unsortedPageable);

        List<Long> threadIds = threads.map(ThreadEntity::getId).toList();

        Map<Long, MessageEntity> latestByThread = messageRepository
                .findLatestVisibleMessagePerThread(threadIds, userId)
                .stream()
                .collect(Collectors.toMap(m -> m.getThread().getId(), m -> m));

        Map<Long, LocalDateTime> lastReadByThread = threadParticipantRepository
                .findReadStatesForThreads(threadIds, userId)
                .stream()
                .collect(Collectors.toMap(ThreadReadState::getThreadId, ThreadReadState::getLastReadAt));

        return threads.map(thread -> {
            MessageEntity latest = latestByThread.get(thread.getId());

            String otherUsername = "";
            Long otherId = null;

            if (latest != null) {
                boolean isSender = latest.getSender().getId().equals(userId);
                otherUsername = isSender
                        ? latest.getRecipient().getUsername()
                        : latest.getSender().getUsername();
                otherId = isSender
                        ? latest.getRecipient().getId()
                        : latest.getSender().getId();
            }

            LocalDateTime lastReadAt = lastReadByThread.get(thread.getId());
            boolean unread = latest != null
                    && !latest.getSender().getId().equals(userId)
                    && (lastReadAt == null || latest.getCreatedAt().isAfter(lastReadAt));

            return new ThreadSummaryResponse(
                    thread.getId(),
                    thread.getSubject(),
                    otherUsername,
                    otherId,
                    latest != null ? latest.getContent().substring(0, Math.min(50, latest.getContent().length())) : "",
                    latest != null ? latest.getCreatedAt() : null,
                    thread.isActive(),
                    unread
            );
        });
    }

    public Page<MessageResponse> getThreadMessages(Long threadId, Long userId, Pageable pageable) {
        if (!messageRepository.hasVisibleMessageInThread(threadId, userId)) {
            throw new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation");
        }

        Page<MessageResponse> messages = messageRepository.findVisibleMessagesByThreadId(threadId, userId, pageable)
                .map(messageMapper::toResponse);

        markThreadReadIfNeeded(threadId, userId);

        return messages;
    }

    private void markThreadReadIfNeeded(Long threadId, Long userId) {
        LocalDateTime latestMessageAt = messageRepository.findLatestMessageTimestamp(threadId, userId);
        if (latestMessageAt == null) {
            return;
        }

        Optional<ThreadParticipantEntity> participant =
                threadParticipantRepository.findByThread_IdAndUser_Id(threadId, userId);

        boolean needsUpdate = participant.isEmpty()
                || participant.get().getLastReadAt() == null
                || participant.get().getLastReadAt().isBefore(latestMessageAt);

        if (needsUpdate) {
            markThreadRead(threadId, userId);
        }
    }

    @Transactional
    public void markThreadRead(Long threadId, Long userId) {
        int updated = threadParticipantRepository.markRead(threadId, userId, LocalDateTime.now());
        if (updated == 0) {
            // Participant row missing (pre-feature thread the backfill didn't cover, or
            // a race with sendMessage). Create it rather than silently no-op.
            ThreadEntity thread = threadRepository.findById(threadId)
                    .orElseThrow(() -> new GlobalExceptionHandler.ThreadNotFoundException(threadId));
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));
            threadParticipantRepository.save(new ThreadParticipantEntity(thread, user, LocalDateTime.now()));
        }
    }

    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new GlobalExceptionHandler.MessageNotFoundException(messageId));

        if (message.getSender().getId().equals(userId)) {
            message.setDeletedBySender(true);
        } else if (message.getRecipient().getId().equals(userId)) {
            message.setDeletedByRecipient(true);
        } else {
            throw new GlobalExceptionHandler.UnauthorizedException("You do not own this message");
        }

        if (message.isDeletedBySender() && message.isDeletedByRecipient()) {
            messageRepository.delete(message);
        } else {
            messageRepository.save(message);
        }
    }

    @Transactional
    public void deleteThreadForUser(Long threadId, Long userId) {
        boolean participant = messageRepository.isUserParticipantInThread(threadId, userId);
        if (!participant) {
            throw new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation");
        }

        messageRepository.markDeletedBySenderForThread(threadId, userId);
        messageRepository.markDeletedByRecipientForThread(threadId, userId);
        messageRepository.deleteFullyDeletedMessagesInThread(threadId);

        if (!messageRepository.existsByThreadId(threadId)) {
            threadRepository.deleteById(threadId);
        }
    }

    public boolean hasUnreadMessages(Long userId) {
        return threadParticipantRepository.existsUnreadForUser(userId);
    }
}