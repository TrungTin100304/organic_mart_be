package com.bryan.dto.response;

import com.bryan.entity.ShipmentStatus;

import java.time.LocalDateTime;

public record ShipmentTrackingResponse(
    Long id,
    ShipmentStatus status,
    String note,
    String location,
    LocalDateTime loggedAt
) {}
