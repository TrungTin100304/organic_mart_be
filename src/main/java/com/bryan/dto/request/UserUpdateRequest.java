package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record UserUpdateRequest(
    @NotBlank String fullName,
    String phoneNumber,
    MultipartFile avatar
) {}
