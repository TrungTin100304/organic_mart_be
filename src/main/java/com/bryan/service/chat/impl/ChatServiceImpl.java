package com.bryan.service.chat.impl;

import com.bryan.dto.response.chat.ChatConversationResponse;
import com.bryan.dto.response.chat.ChatMessageResponse;
import com.bryan.entity.User;
import com.bryan.entity.chat.*;
import com.bryan.exception.ForbiddenException;
import com.bryan.exception.NotFoundException;
import com.bryan.repository.UserRepository;
import com.bryan.repository.chat.ChatConversationRepository;
import com.bryan.repository.chat.ChatMessageRepository;
import com.bryan.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Override
    public ChatConversationResponse getOrCreateConversation(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return conversationRepository.findByUserIdAndStatus(userId, ChatConversationStatus.OPEN)
                .map(this::toConversationResponse)
                .orElseGet(() -> {
                    ChatConversation conversation = ChatConversation.builder()
                            .user(user)
                            .status(ChatConversationStatus.OPEN)
                            .build();
                    return toConversationResponse(conversationRepository.save(conversation));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationResponse getCurrentConversation(Long userId) {
        ChatConversation conversation = conversationRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public ChatConversationResponse getConversationById(Long conversationId, Long userId) {
        ChatConversation conversation = conversationRepository.findByIdWithUser(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (!conversation.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        return toConversationResponse(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getMessages(Long conversationId, Long userId, Pageable pageable) {
        ChatConversation conversation = conversationRepository.findByIdWithUser(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (!conversation.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toMessageResponse);
    }

    @Override
    public ChatMessageResponse sendMessage(Long conversationId, Long senderId, String content, String clientMessageId) {
        ChatConversation conversation = conversationRepository.findByIdWithUser(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (conversation.getStatus() == ChatConversationStatus.CLOSED) {
            throw new ForbiddenException("Cannot send message to a closed conversation");
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (messageRepository.existsByConversationIdAndClientMessageId(conversationId, clientMessageId)) {
            log.debug("Duplicate message detected: conversationId={}, clientMessageId={}", conversationId, clientMessageId);
            return messageRepository.findByConversationIdAndClientMessageId(conversationId, clientMessageId)
                    .map(this::toMessageResponse)
                    .orElseThrow();
        }

        ChatMessageSenderRole senderRole = sender.getRole() == com.bryan.entity.Role.ROLE_ADMIN
                ? ChatMessageSenderRole.ADMIN
                : ChatMessageSenderRole.USER;

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .sender(sender)
                .senderRole(senderRole)
                .content(content.trim())
                .status(ChatMessageStatus.SENT)
                .clientMessageId(clientMessageId)
                .build();

        ChatMessage saved = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        log.info("Message sent: conversationId={}, senderId={}, role={}", conversationId, senderId, senderRole);
        return toMessageResponse(saved);
    }

    @Override
    public ChatMessageResponse markAsRead(Long conversationId, Long userId, boolean isAdmin) {
        ChatConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        if (!conversation.getUser().getId().equals(userId) && !isAdmin) {
            throw new ForbiddenException("You don't have access to this conversation");
        }

        if (isAdmin) {
            messageRepository.markAdminMessagesAsRead(conversationId, ChatMessageStatus.READ);
        } else {
            messageRepository.markUserMessagesAsRead(conversationId, ChatMessageStatus.READ);
        }

        log.debug("Messages marked as read: conversationId={}, byAdmin={}", conversationId, isAdmin);
        return null;
    }

    @Override
    public ChatConversationResponse closeConversation(Long conversationId, Long adminId) {
        ChatConversation conversation = conversationRepository.findByIdWithUser(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        conversation.setStatus(ChatConversationStatus.CLOSED);
        conversation.setUpdatedAt(LocalDateTime.now());
        ChatConversation saved = conversationRepository.save(conversation);

        String systemMessage = "Cuộc trò chuyện đã được đóng bởi quản trị viên.";
        createSystemMessage(conversation, systemMessage);

        log.info("Conversation closed: conversationId={}, byAdminId={}", conversationId, adminId);
        return toConversationResponse(saved);
    }

    @Override
    public ChatConversationResponse reopenConversation(Long conversationId, Long adminId) {
        ChatConversation conversation = conversationRepository.findByIdWithUser(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found"));

        conversation.setStatus(ChatConversationStatus.OPEN);
        conversation.setUpdatedAt(LocalDateTime.now());
        ChatConversation saved = conversationRepository.save(conversation);

        String systemMessage = "Cuộc trò chuyện đã được mở lại bởi quản trị viên.";
        createSystemMessage(conversation, systemMessage);

        log.info("Conversation reopened: conversationId={}, byAdminId={}", conversationId, adminId);
        return toConversationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatConversationResponse> getAdminConversations(String status, Pageable pageable) {
        Page<ChatConversation> conversations;
        if (status != null && !status.isBlank()) {
            ChatConversationStatus conversationStatus = ChatConversationStatus.valueOf(status.toUpperCase());
            conversations = conversationRepository.findByStatusOrderByUpdatedAtDesc(conversationStatus, pageable);
        } else {
            conversations = conversationRepository.findAllOrderByUpdatedAtDesc(pageable);
        }
        return conversations.map(this::toConversationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getAdminMessages(Long conversationId, Pageable pageable) {
        if (!conversationRepository.existsById(conversationId)) {
            throw new NotFoundException("Conversation not found");
        }
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId, pageable)
                .map(this::toMessageResponse);
    }

    private void createSystemMessage(ChatConversation conversation, String content) {
        ChatMessage systemMessage = ChatMessage.builder()
                .conversation(conversation)
                .senderRole(ChatMessageSenderRole.SYSTEM)
                .content(content)
                .status(ChatMessageStatus.SENT)
                .build();
        messageRepository.save(systemMessage);
    }

    private ChatConversationResponse toConversationResponse(ChatConversation conversation) {
        return ChatConversationResponse.builder()
                .id(conversation.getId())
                .userId(conversation.getUser().getId())
                .userEmail(conversation.getUser().getEmail())
                .userFullName(conversation.getUser().getFullName())
                .status(conversation.getStatus())
                .lastMessageAt(conversation.getLastMessageAt())
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .conversationId(message.getConversation().getId())
                .senderId(message.getSender() != null ? message.getSender().getId() : null)
                .senderEmail(message.getSender() != null ? message.getSender().getEmail() : null)
                .senderRole(message.getSenderRole())
                .content(message.getContent())
                .status(message.getStatus())
                .clientMessageId(message.getClientMessageId())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
