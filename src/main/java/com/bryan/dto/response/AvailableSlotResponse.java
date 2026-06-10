package com.bryan.dto.response;

import java.time.LocalTime;

public record AvailableSlotResponse(
    Long slotId,
    String name,
    LocalTime startTime,
    LocalTime endTime,
    int remainingCapacity,
    boolean available,
    String unavailableReason
) {}
