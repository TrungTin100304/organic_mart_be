package com.bryan.dto.request;

public record UserStatusRequest(
    Boolean isActive,
    Boolean active,
    String status
) {}
