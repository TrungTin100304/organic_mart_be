package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FarmRequest(
    @NotBlank(message = "Farm name is required")
    String name,

    String certification,

    String location,

    String contactPhone,

    String contactEmail
) {}

