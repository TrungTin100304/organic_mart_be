package com.bryan.dto.response;

import com.bryan.entity.OrderDetail;

import java.math.BigDecimal;

public record OrderDetailResponse(
    Long id,
    Long productId,
    String productName,
    String productSlug,
    String imageUrl,
    Long batchId,
    String batchCode,
    BigDecimal quantity,
    String unit,
    BigDecimal priceAtPurchase,
    BigDecimal lineSubtotal
) {
    public static OrderDetailResponse from(OrderDetail detail) {
        return new OrderDetailResponse(
            detail.getId(),
            detail.getProduct() != null ? detail.getProduct().getId() : null,
            detail.getProduct() != null ? detail.getProduct().getName() : null,
            detail.getProduct() != null ? detail.getProduct().getSlug() : null,
            detail.getProduct() != null ? detail.getProduct().getImageUrl() : null,
            detail.getBatch() != null ? detail.getBatch().getId() : null,
            detail.getBatch() != null ? detail.getBatch().getBatchCode() : null,
            detail.getQuantity(),
            detail.getProduct() != null ? detail.getProduct().getUnit() : null,
            detail.getPriceAtPurchase(),
            detail.getLineSubtotal()
        );
    }
}
