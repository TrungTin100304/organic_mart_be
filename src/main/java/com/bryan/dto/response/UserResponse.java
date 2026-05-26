package com.bryan.dto.response;

import com.bryan.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String fullName,
    String phoneNumber,
    String email,
    String avatarUrl,
    boolean isActive,
    Role role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

