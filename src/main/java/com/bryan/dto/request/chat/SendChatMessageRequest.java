package com.bryan.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendChatMessageRequest(
        @NotBlank(message = "content is required")
        @Size(max = 2000, message = "content must not exceed 2000 characters")
        String content,

        @Size(max = 120, message = "clientMessageId must not exceed 120 characters")
        String clientMessageId
) {}
