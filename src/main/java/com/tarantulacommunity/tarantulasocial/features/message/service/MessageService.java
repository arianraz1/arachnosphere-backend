package com.tarantulacommunity.tarantulasocial.features.message.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.mapper.MessageMapper;
import com.tarantulacommunity.tarantulasocial.features.message.repository.MessageRepository;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;

    @Transactional
    public MessageResponse sendMessage(Long senderId, SendMessageRequest request) {
        UserEntity sender = userRepository.findById(senderId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(senderId));

        UserEntity recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(request.getRecipientId()));

        if (sender.getId().equals(recipient.getId())) {
            throw new GlobalExceptionHandler.InvalidMessageException("You cannot send a message to yourself");
        }

        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(request.getContent());

        return messageMapper.toResponse(messageRepository.save(message));
    }

    public Page<MessageResponse> getInbox(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));

        return messageRepository
                .findByRecipientAndDeletedByRecipientFalseOrderByCreatedAtDesc(user, pageable)
                .map(messageMapper::toResponse);
    }

    public Page<MessageResponse> getSent(Long userId, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));

        return messageRepository
                .findBySenderAndDeletedBySenderFalseOrderByCreatedAtDesc(user, pageable)
                .map(messageMapper::toResponse);
    }

    @Transactional
    public void deleteFromInbox(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new GlobalExceptionHandler.MessageNotFoundException(messageId));

        if (!message.getRecipient().getId().equals(userId)) {
            throw new GlobalExceptionHandler.UnauthorizedException("You do not own this message");
        }

        message.setDeletedByRecipient(true);
        messageRepository.save(message);
    }

    @Transactional
    public void deleteFromSent(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findById(messageId)
                .orElseThrow(() -> new GlobalExceptionHandler.MessageNotFoundException(messageId));

        if (!message.getSender().getId().equals(userId)) {
            throw new GlobalExceptionHandler.UnauthorizedException("You do not own this message");
        }

        message.setDeletedBySender(true);
        messageRepository.save(message);
    }
}
