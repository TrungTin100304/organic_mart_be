package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record UserUpdateRequest(
    @NotBlank(message = "Họ tên là bắt buộc")
    String fullName,
    String phoneNumber,
    MultipartFile avatar,
    Boolean isActive,
    Boolean active,
    String status
) {}
