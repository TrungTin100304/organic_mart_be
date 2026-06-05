package com.bryan.dto.request;

import com.bryan.entity.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddTrackingRequest(
    @NotNull(message = "Status is required")
    ShipmentStatus status,

    @Size(max = 200, message = "Location không được vượt quá 200 ký tự")
    String location,

    String note
) {}
