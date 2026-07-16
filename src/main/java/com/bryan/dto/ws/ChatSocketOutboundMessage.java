package com.bryan.dto.ws;

import com.bryan.entity.chat.ChatMessageSenderRole;
import com.bryan.entity.chat.ChatMessageStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSocketOutboundMessage {

    public enum MessageType {
        CHAT_MESSAGE,
        MESSAGE_READ,
        CONVERSATION_UPDATED,
        ERROR,
        CONNECTION_ACK
    }

    private MessageType type;
    private Long messageId;
    private Long conversationId;
    private Long senderId;
    private String senderEmail;
    private ChatMessageSenderRole senderRole;
    private String content;
    private ChatMessageStatus status;
    private String clientMessageId;
    private LocalDateTime createdAt;
    private String errorMessage;
}
