package com.bryan.dto.response;

import java.time.LocalDateTime;

public record AllergenResponse(
    Long id,
    String name,
    LocalDateTime createdAt
) {}

