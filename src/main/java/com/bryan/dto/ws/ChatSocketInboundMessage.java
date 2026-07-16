package com.bryan.dto.ws;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSocketInboundMessage {

    @NotBlank(message = "Message type is required")
    private String type;

    @NotNull(message = "Conversation ID is required")
    private Long conversationId;

    @NotBlank(message = "Content is required")
    @Size(max = 2000, message = "Content must not exceed 2000 characters")
    private String content;

    @Size(max = 120)
    private String clientMessageId;

    private String sentAt;
}
