package com.bryan.dto.request.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSendMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được trống")
    @Size(max = 2000, message = "Tin nhắn không được vượt quá 2000 ký tự")
    private String content;
}
