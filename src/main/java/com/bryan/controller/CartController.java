    package com.bryan.controller;

import com.bryan.dto.request.AddCartItemRequest;
import com.bryan.dto.request.UpdateCartItemQuantityRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.CartResponse;
import com.bryan.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Carts", description = "Shopping cart management endpoints")
public class CartController {

    private final CartService cartService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get the current user's cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCurrentCart() {
        return ApiResponse.success(cartService.getCurrentCart());
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add a product to the current cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(@Valid @RequestBody AddCartItemRequest request) {
        return ApiResponse.success(cartService.addItem(request));
    }

    @PatchMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update quantity for an item in the current cart")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
        @PathVariable Long productId,
        @Valid @RequestBody UpdateCartItemQuantityRequest request
    ) {
        return ApiResponse.success(cartService.updateItemQuantity(productId, request));
    }

    @DeleteMapping("/items/{productId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove a product from the current cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(@PathVariable Long productId) {
        return ApiResponse.success(cartService.removeItem(productId));
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Clear the current cart")
    public ResponseEntity<ApiResponse<CartResponse>> clearCart() {
        return ApiResponse.success(cartService.clearCart());
    }
}

