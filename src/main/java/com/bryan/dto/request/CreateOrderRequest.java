package com.bryan.dto.request;

import com.bryan.entity.DeliveryMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Address ID is required")
    Long addressId,

    String promotionCode,

    String note,

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItemRequest> items,

    // Internal delivery
    @NotNull(message = "Delivery method is required")
    DeliveryMethod deliveryMethod,

    LocalDate deliveryDate,

    Long deliverySlotId
) {}
