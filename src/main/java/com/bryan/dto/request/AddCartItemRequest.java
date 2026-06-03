package com.bryan.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record AddCartItemRequest(
    @NotNull(message = "ID sản phẩm là bắt buộc")
    Long productId,

    @NotNull(message = "Số lượng là bắt buộc")
    @Positive(message = "Số lượng phải lớn hơn 0")
    BigDecimal quantity
) {}
