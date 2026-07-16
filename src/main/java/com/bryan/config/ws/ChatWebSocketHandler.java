package com.bryan.config.ws;

import com.bryan.dto.response.chat.ChatMessageResponse;
import com.bryan.dto.ws.ChatSocketInboundMessage;
import com.bryan.dto.ws.ChatSocketOutboundMessage;
import com.bryan.entity.chat.ChatConversationStatus;
import com.bryan.entity.chat.ChatMessageSenderRole;
import com.bryan.entity.chat.ChatMessageStatus;
import com.bryan.exception.ForbiddenException;
import com.bryan.exception.NotFoundException;
import com.bryan.repository.chat.ChatConversationRepository;
import com.bryan.service.chat.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatSessionRegistry sessionRegistry;
    private final ChatService chatService;
    private final ChatConversationRepository conversationRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = sessionRegistry.getUserId(session);
        String email = sessionRegistry.getUserEmail(session);
        boolean isAdmin = sessionRegistry.isAdmin(session);

        sessionRegistry.register(session, userId, email, isAdmin);

        ChatSocketOutboundMessage ack = ChatSocketOutboundMessage.builder()
                .type(ChatSocketOutboundMessage.MessageType.CONNECTION_ACK)
                .content("Connected successfully")
                .build();

        try {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(ack)));
        } catch (Exception e) {
            log.error("Failed to send connection ack: {}", e.getMessage());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = sessionRegistry.getUserId(session);
        boolean isAdmin = sessionRegistry.isAdmin(session);

        if (userId == null) {
            log.warn("Received message from session without userId: {}", session.getId());
            sendError(session, "Not authenticated");
            return;
        }

        try {
            ChatSocketInboundMessage inbound = objectMapper.readValue(
                    message.getPayload(), ChatSocketInboundMessage.class);

            if (!"CHAT_MESSAGE".equals(inbound.getType())) {
                sendError(session, "Unknown message type: " + inbound.getType());
                return;
            }

            handleChatMessage(session, userId, isAdmin, inbound);

        } catch (Exception e) {
            log.error("Error handling WebSocket message: {}", e.getMessage());
            sendError(session, "Invalid message format");
        }
    }

    private void handleChatMessage(WebSocketSession session, Long userId, boolean isAdmin,
                                   ChatSocketInboundMessage inbound) throws Exception {
        Long conversationId = inbound.getConversationId();
        String content = inbound.getContent();
        String clientMessageId = inbound.getClientMessageId();

        if (content == null || content.isBlank()) {
            sendError(session, "Content cannot be empty");
            return;
        }

        content = content.trim();
        if (content.length() > 2000) {
            sendError(session, "Content exceeds maximum length of 2000 characters");
            return;
        }

        var conversationOpt = conversationRepository.findById(conversationId);
        if (conversationOpt.isEmpty()) {
            sendError(session, "Conversation not found");
            return;
        }

        var conversation = conversationOpt.get();

        if (conversation.getStatus() == ChatConversationStatus.CLOSED) {
            sendError(session, "Conversation is closed");
            return;
        }

        if (!isAdmin && !conversation.getUser().getId().equals(userId)) {
            sendError(session, "Access denied to this conversation");
            return;
        }

        try {
            ChatMessageResponse savedMessage = chatService.sendMessage(
                    conversationId, userId, content, clientMessageId);

            ChatSocketOutboundMessage outbound = ChatSocketOutboundMessage.builder()
                    .type(ChatSocketOutboundMessage.MessageType.CHAT_MESSAGE)
                    .messageId(savedMessage.getId())
                    .conversationId(savedMessage.getConversationId())
                    .senderId(savedMessage.getSenderId())
                    .senderEmail(savedMessage.getSenderEmail())
                    .senderRole(savedMessage.getSenderRole())
                    .content(savedMessage.getContent())
                    .status(savedMessage.getStatus())
                    .clientMessageId(savedMessage.getClientMessageId())
                    .createdAt(savedMessage.getCreatedAt())
                    .build();

            String jsonMessage = objectMapper.writeValueAsString(outbound);

            sessionRegistry.sendToUser(conversation.getUser().getId(), new TextMessage(jsonMessage));

            sessionRegistry.broadcastToAdmins(new TextMessage(jsonMessage));

        } catch (ForbiddenException | NotFoundException e) {
            sendError(session, e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessionRegistry.unregister(session);
        log.info("WebSocket connection closed: sessionId={}, status={}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: sessionId={}, error={}", session.getId(), exception.getMessage());
        sessionRegistry.unregister(session);
    }

    private void sendError(WebSocketSession session, String errorMessage) {
        try {
            ChatSocketOutboundMessage error = ChatSocketOutboundMessage.builder()
                    .type(ChatSocketOutboundMessage.MessageType.ERROR)
                    .errorMessage(errorMessage)
                    .build();
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(error)));
        } catch (Exception e) {
            log.error("Failed to send error message: {}", e.getMessage());
        }
    }
}
