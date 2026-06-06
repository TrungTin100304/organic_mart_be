package com.bryan.dto.response;

import java.time.LocalDateTime;

public record ShippingProviderResponse(
    Long id,
    String name,
    Boolean isActive,
    LocalDateTime createdAt
) {}
