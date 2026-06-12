package com.bryan.dto.request;

import jakarta.validation.constraints.NotNull;

public record StatusRequest(
    @NotNull(message = "isActive status is required")
    Boolean isActive
) {}
