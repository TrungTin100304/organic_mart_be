package com.bryan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Address ID is required")
    Long addressId,

    String promotionCode,

    @Size(max = 500, message = "Note must not exceed 500 characters")
    String note,

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItemRequest> items
) {}
