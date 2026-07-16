package com.bryan.config.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ChatSessionRegistry {

    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> sessionById = new ConcurrentHashMap<>();

    public void register(WebSocketSession session, Long userId, String email, boolean isAdmin) {
        userSessions.put(userId, session);
        sessionById.put(session.getId(), session);
        log.info("WebSocket session registered: userId={}, email={}, isAdmin={}, sessionId={}",
                userId, email, isAdmin, session.getId());
    }

    public void unregister(WebSocketSession session) {
        Long userId = getUserId(session);
        if (userId != null) {
            userSessions.remove(userId);
            log.info("WebSocket session unregistered: userId={}, sessionId={}", userId, session.getId());
        }
        sessionById.remove(session.getId());
    }

    public WebSocketSession getSession(Long userId) {
        return userSessions.get(userId);
    }

    public WebSocketSession getSessionById(String sessionId) {
        return sessionById.get(sessionId);
    }

    public Set<WebSocketSession> getAllAdminSessions() {
        return sessionById.values().stream()
                .filter(this::isAdmin)
                .collect(java.util.stream.Collectors.toSet());
    }

    public Set<WebSocketSession> getAllSessions() {
        return Set.copyOf(sessionById.values());
    }

    public boolean isAdmin(WebSocketSession session) {
        Object isAdminAttr = session.getAttributes().get("isAdmin");
        return Boolean.TRUE.equals(isAdminAttr);
    }

    public Long getUserId(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr instanceof Long) {
            return (Long) userIdAttr;
        } else if (userIdAttr instanceof Integer) {
            return ((Integer) userIdAttr).longValue();
        }
        return null;
    }

    public String getUserEmail(WebSocketSession session) {
        Object emailAttr = session.getAttributes().get("email");
        return emailAttr != null ? emailAttr.toString() : null;
    }

    public void sendToUser(Long userId, TextMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                synchronized (session) {
                    session.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("Failed to send message to user {}: {}", userId, e.getMessage());
            }
        }
    }

    public void broadcastToAdmins(TextMessage message) {
        getAllAdminSessions().forEach(session -> {
            if (session.isOpen()) {
                try {
                    synchronized (session) {
                        session.sendMessage(message);
                    }
                } catch (Exception e) {
                    log.error("Failed to broadcast to admin session {}: {}", session.getId(), e.getMessage());
                }
            }
        });
    }

    public void sendToConversation(Long conversationUserId, TextMessage message) {
        sendToUser(conversationUserId, message);
        broadcastToAdmins(message);
    }
}
