package com.bryan.mapper;

import com.bryan.dto.response.CartItemResponse;
import com.bryan.dto.response.CartResponse;
import com.bryan.entity.Cart;
import com.bryan.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface CartMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.slug", target = "productSlug")
    @Mapping(source = "product.imageUrl", target = "imageUrl")
    @Mapping(source = "product.price", target = "unitPrice")
    @Mapping(source = "product.unit", target = "unit")
    @Mapping(target = "subtotal", expression = "java(calculateSubtotal(item))")
    CartItemResponse toResponse(CartItem item);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(target = "totalQuantity", expression = "java(calculateTotalQuantity(cart))")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart))")
    @Mapping(target = "distinctItemCount", expression = "java(cart.getItems() == null ? 0 : cart.getItems().size())")
    CartResponse toResponse(Cart cart);

    default BigDecimal calculateSubtotal(CartItem item) {
        if (item == null || item.getQuantity() == null || item.getProduct() == null || item.getProduct().getPrice() == null) {
            return BigDecimal.ZERO;
        }
        return item.getQuantity().multiply(item.getProduct().getPrice());
    }

    default BigDecimal calculateTotalQuantity(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
            .map(CartItem::getQuantity)
            .filter(quantity -> quantity != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default BigDecimal calculateTotalPrice(Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }
        return cart.getItems().stream()
            .map(this::calculateSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
