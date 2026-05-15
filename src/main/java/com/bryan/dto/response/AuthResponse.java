package com.bryan.dto.response;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        String email,
        String role
) {}
