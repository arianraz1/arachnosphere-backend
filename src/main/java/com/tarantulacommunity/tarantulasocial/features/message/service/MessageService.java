package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.message.repository.MessageRepository;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import com.tarantulacommunity.tarantulasocial.features.message.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;
    private final ThreadRepository threadRepository;

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
        if (request.getThreadId() != null) {
            thread = threadRepository.findById(request.getThreadId())
                    .orElseThrow(() -> new GlobalExceptionHandler.ThreadNotFoundException(request.getThreadId()));

            if (!thread.isActive()) {
                throw new GlobalExceptionHandler.InvalidMessageException("This thread is no longer active");
            }

            if (!messageRepository.existsByThreadIdAndUserId(thread.getId(), senderId)) {
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

        return messageMapper.toResponse(saved);
    }

    public Page<ThreadSummaryResponse> getThreadsForUser(Long userId, Pageable pageable) {
        Page<ThreadEntity> threads = messageRepository.findThreadsByUserId(userId, pageable);

        List<Long> threadIds = threads.map(ThreadEntity::getId).toList();

        Map<Long, MessageEntity> latestByThread = messageRepository
                .findLatestMessagePerThread(threadIds)
                .stream()
                .collect(Collectors.toMap(m -> m.getThread().getId(), m -> m));

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

            return new ThreadSummaryResponse(
                    thread.getId(),
                    thread.getSubject(),
                    otherUsername,
                    otherId,
                    latest != null ? latest.getContent().substring(0, Math.min(50, latest.getContent().length())) : "",
                    latest != null ? latest.getCreatedAt() : null,
                    thread.isActive()
            );
        });
    }

    public List<MessageResponse> getThreadMessages(Long threadId, Long userId) {
        if (!messageRepository.existsByThreadIdAndUserId(threadId, userId)) {
            throw new GlobalExceptionHandler.UnauthorizedException("You are not part of this conversation");
        }

        return messageRepository.findByThreadIdOrderByCreatedAtAsc(threadId)
                .stream()
                .map(messageMapper::toResponse)
                .collect(Collectors.toList());
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
}