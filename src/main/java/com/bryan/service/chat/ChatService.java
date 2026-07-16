package com.bryan.service.chat;

import com.bryan.dto.response.chat.ChatConversationResponse;
import com.bryan.dto.response.chat.ChatMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatService {

    ChatConversationResponse getOrCreateConversation(Long userId);

    ChatConversationResponse getCurrentConversation(Long userId);

    ChatConversationResponse getConversationById(Long conversationId, Long userId);

    Page<ChatMessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable);

    ChatMessageResponse sendMessage(Long conversationId, Long senderId, String content, String clientMessageId);

    ChatMessageResponse markAsRead(Long conversationId, Long userId, boolean isAdmin);

    ChatConversationResponse closeConversation(Long conversationId, Long adminId);

    ChatConversationResponse reopenConversation(Long conversationId, Long adminId);

    Page<ChatConversationResponse> getAdminConversations(String status, Pageable pageable);

    Page<ChatMessageResponse> getAdminMessages(Long conversationId, Pageable pageable);
}
