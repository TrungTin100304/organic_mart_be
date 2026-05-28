package com.bryan.service;

import com.bryan.dto.request.AddCartItemRequest;
import com.bryan.dto.request.UpdateCartItemQuantityRequest;
import com.bryan.dto.response.CartResponse;

public interface CartService {
    CartResponse getCurrentCart();
    CartResponse addItem(AddCartItemRequest request);
    CartResponse updateItemQuantity(Long productId, UpdateCartItemQuantityRequest request);
    CartResponse removeItem(Long productId);
    CartResponse clearCart();
}

