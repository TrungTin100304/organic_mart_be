package com.bryan.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryBatchRequest(
    @NotNull(message = "ID sản phẩm là bắt buộc")
    Long productId,

    @NotNull(message = "ID trang trại là bắt buộc")
    Long farmId,

    @NotBlank(message = "Mã lô hàng là bắt buộc")
    String batchCode,

    @NotNull(message = "Số lượng ban đầu là bắt buộc")
    @Positive(message = "Số lượng ban đầu phải lớn hơn 0")
    BigDecimal quantityInitial,

    @NotNull(message = "Số lượng còn lại là bắt buộc")
    @PositiveOrZero(message = "Số lượng còn lại phải lớn hơn hoặc bằng 0")
    BigDecimal quantityRemaining,

    @NotNull(message = "Ngày nhập kho là bắt buộc")
    LocalDate importDate,

    @NotNull(message = "Ngày hết hạn là bắt buộc")
    LocalDate expiryDate,

    BigDecimal costPrice
) {}
