package com.bryan.dto.response;

import java.time.LocalDateTime;

public record FarmResponse(
    Long id,
    String name,
    String certification,
    String location,
    String contactPhone,
    String contactEmail,
    LocalDateTime createdAt
) {}

