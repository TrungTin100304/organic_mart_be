package com.bryan.dto.response.chat;

import com.bryan.entity.chat.ChatMessageSenderRole;
import com.bryan.entity.chat.ChatMessageStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderEmail;
    private ChatMessageSenderRole senderRole;
    private String content;
    private ChatMessageStatus status;
    private String clientMessageId;
    private LocalDateTime createdAt;
}
