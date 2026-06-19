package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ---------- PROTECTED — all message endpoints require authentication ----------

    // Send a message — recipient is specified in the request body
    // Frontend populates recipientId from the profile page the user is visiting
    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SendMessageRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(userId, request));
    }

    @GetMapping("/inbox")
    public ResponseEntity<Page<MessageResponse>> inbox(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(messageService.getInbox(userId, pageable));
    }

    @GetMapping("/sent")
    public ResponseEntity<Page<MessageResponse>> sent(
            @AuthenticationPrincipal Long userId,
            @PageableDefault(size = 20) Pageable pageable) {

        return ResponseEntity.ok(messageService.getSent(userId, pageable));
    }

    @DeleteMapping("/inbox/{messageId}")
    public ResponseEntity<Void> deleteFromInbox(
            @PathVariable Long messageId,
            @AuthenticationPrincipal Long userId) {

        messageService.deleteFromInbox(messageId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sent/{messageId}")
    public ResponseEntity<Void> deleteFromSent(
            @PathVariable Long messageId,
            @AuthenticationPrincipal Long userId) {

        messageService.deleteFromSent(messageId, userId);
        return ResponseEntity.noContent().build();
    }
}