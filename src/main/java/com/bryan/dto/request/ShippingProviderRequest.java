package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ShippingProviderRequest(
    @NotBlank(message = "Tên nhà vận chuyển là bắt buộc")
    @Size(max = 100, message = "Tên nhà vận chuyển không được vượt quá 100 ký tự")
    String name
) {}
