package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
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

    @PostMapping
    public ResponseEntity<MessageResponse> send(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(userDetails.getId(), request));
    }

    @GetMapping("/inbox")
    public ResponseEntity<Page<MessageResponse>> inbox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.getInbox(userDetails.getId(), pageable));
    }

    @GetMapping("/sent")
    public ResponseEntity<Page<MessageResponse>> sent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.getSent(userDetails.getId(), pageable));
    }

    @DeleteMapping("/inbox/{messageId}")
    public ResponseEntity<Void> deleteFromInbox(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        messageService.deleteFromInbox(messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/sent/{messageId}")
    public ResponseEntity<Void> deleteFromSent(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        messageService.deleteFromSent(messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}