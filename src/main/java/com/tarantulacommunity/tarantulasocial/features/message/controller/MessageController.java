package com.tarantulacommunity.tarantulasocial.features.message.controller;

import com.tarantulacommunity.tarantulasocial.features.message.dto.request.CreateThreadRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.request.ReplyRequest;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.MessageResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadStatusResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.ThreadSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.message.dto.response.UnreadStatusResponse;
import com.tarantulacommunity.tarantulasocial.features.message.service.MessageService;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // Send a new message or reply to an existing thread
    @PostMapping("/threads")
    public ResponseEntity<MessageResponse> createThread(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateThreadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.createThread(userDetails.getId(), request));
    }

    @PostMapping("/{threadId}/replies")
    public ResponseEntity<MessageResponse> reply(
            @PathVariable Long threadId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ReplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.replyToThread(userDetails.getId(), threadId, request));
    }

    // Inbox: list of threads
    @GetMapping("/inbox")
    public ResponseEntity<Page<ThreadSummaryResponse>> inbox(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(messageService.getThreadsForUser(userDetails.getId(), pageable));
    }

    // Open a thread: all messages in it
    @GetMapping("/inbox/thread/{threadId}")
    public ResponseEntity<Page<MessageResponse>> getThread(
            @PathVariable Long threadId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(messageService.getThreadMessages(threadId, userDetails.getId(), pageable));
    }

    // Check if a thread is active
    @GetMapping("/inbox/thread/{threadId}/status")
    public ResponseEntity<ThreadStatusResponse> getThreadStatus(
            @PathVariable Long threadId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(messageService.getThreadStatus(threadId, userDetails.getId()));
    }

    // Soft delete a message from your view
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable Long messageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        messageService.deleteMessage(messageId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // Soft delete an entire thread from your view
    @DeleteMapping("/thread/{threadId}")
    public ResponseEntity<Void> deleteThreadForMe(
            @PathVariable Long threadId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        messageService.deleteThreadForUser(threadId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    // Lightweight check for nav badge — does the user have any unread thread at all?
    @GetMapping("/unread")
    public ResponseEntity<UnreadStatusResponse> hasUnread(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(new UnreadStatusResponse(messageService.hasUnreadMessages(userDetails.getId())));
    }
}