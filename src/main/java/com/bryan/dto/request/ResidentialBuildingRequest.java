package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResidentialBuildingRequest(
    @NotBlank(message = "Building code is required")
    @Size(max = 20, message = "Building code must not exceed 20 characters")
    String code,

    @NotBlank(message = "Building name is required")
    @Size(max = 200, message = "Building name must not exceed 200 characters")
    String name,

    String description,

    @jakarta.validation.constraints.Min(value = 0, message = "Display order must be non-negative")
    Integer displayOrder
) {}
