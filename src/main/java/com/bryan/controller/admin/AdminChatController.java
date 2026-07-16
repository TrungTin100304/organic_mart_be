package com.bryan.controller.admin;

import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.chat.ChatConversationResponse;
import com.bryan.dto.response.chat.ChatMessageResponse;
import com.bryan.security.CustomUserDetails;
import com.bryan.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/chat")
@RequiredArgsConstructor
public class AdminChatController {

    private final ChatService chatService;

    @GetMapping("/conversations")
    public ResponseEntity<ApiResponse<Page<ChatConversationResponse>>> getConversations(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ChatConversationResponse> conversations = chatService.getAdminConversations(status, pageable);
        return ApiResponse.success(conversations);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        Pageable pageable = PageRequest.of(page, Math.min(size, 100));
        Page<ChatMessageResponse> messages = chatService.getAdminMessages(id, pageable);
        return ApiResponse.success(messages);
    }

    @PatchMapping("/conversations/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatService.markAsRead(id, userDetails.getId(), true);
        return ApiResponse.success(null, "Messages marked as read");
    }

    @PatchMapping("/conversations/{id}/close")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> closeConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatConversationResponse conversation = chatService.closeConversation(id, userDetails.getId());
        return ApiResponse.success(conversation, "Conversation closed");
    }

    @PatchMapping("/conversations/{id}/reopen")
    public ResponseEntity<ApiResponse<ChatConversationResponse>> reopenConversation(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ChatConversationResponse conversation = chatService.reopenConversation(id, userDetails.getId());
        return ApiResponse.success(conversation, "Conversation reopened");
    }
}
