package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Token là bắt buộc")
        String token,
        @NotBlank(message = "Mật khẩu mới là bắt buộc")
        String newPassword
) {
}
