package com.bryan.dto.response.chat;

import com.bryan.entity.chat.ChatConversationStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatConversationResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private ChatConversationStatus status;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
