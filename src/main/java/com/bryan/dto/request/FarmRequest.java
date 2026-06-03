package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;

public record FarmRequest(
    @NotBlank(message = "Tên trang trại là bắt buộc")
    String name,

    String certification,

    String location,

    String contactPhone,

    String contactEmail
) {}
