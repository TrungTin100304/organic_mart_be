package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AllergenRequest(
    @NotBlank(message = "Tên chất gây dị ứng là bắt buộc")
    String name
) {}

