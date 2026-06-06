package com.bryan.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Address ID is required")
    Long addressId,

    @NotNull(message = "Shipping provider is required")
    Long shippingProviderId,

    @NotNull(message = "Shipping fee is required")
    @DecimalMin(value = "0.0", message = "Shipping fee must be >= 0")
    BigDecimal shippingFee,

    String promotionCode,

    String note,

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItemRequest> items
) {}
