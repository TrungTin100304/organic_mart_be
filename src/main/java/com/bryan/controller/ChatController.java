package com.bryan.controller;

import com.bryan.dto.request.chat.SendChatMessageRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.chat.ChatConversationResponse;
import com.bryan.dto.response.chat.ChatMessageResponse;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.chat.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> getOrCreateConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatConversationResponse conversation = chatService.getOrCreateConversation(userDetails.getId());
        return ApiResponse.success(conversation);
    }

    @GetMapping("/conversations/me")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> getMyConversation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatConversationResponse conversation = chatService.getCurrentConversation(userDetails.getId());
        return ApiResponse.success(conversation);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ChatMessageResponse> messages = chatService.getMessages(id, userDetails.getId(), pageable);
        return ApiResponse.success(messages);
    }

    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long id,
            @Valid @RequestBody SendChatMessageRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatMessageResponse message = chatService.sendMessage(
                id,
                userDetails.getId(),
                request.content(),
                request.clientMessageId());
        return ApiResponse.success(message);
    }

    @PatchMapping("/conversations/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.markAsRead(id, userDetails.getId(), false);
        return ApiResponse.success(null, "Messages marked as read");
    }
}
