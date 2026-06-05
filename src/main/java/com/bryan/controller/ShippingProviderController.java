package com.bryan.controller;

import com.bryan.dto.request.ShippingProviderRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.ShippingProviderResponse;
import com.bryan.service.ShippingProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shipping-providers")
@RequiredArgsConstructor
@Tag(name = "Shipping Providers", description = "Manage shipping providers")
public class ShippingProviderController {

    private final ShippingProviderService providerService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all shipping providers")
    public ResponseEntity<ApiResponse<List<ShippingProviderResponse>>> getAllProviders() {
        return ApiResponse.success(providerService.getAllProviders());
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active shipping providers (public)")
    public ResponseEntity<ApiResponse<List<ShippingProviderResponse>>> getActiveProviders() {
        return ApiResponse.success(providerService.getActiveProviders());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get shipping provider by ID")
    public ResponseEntity<ApiResponse<ShippingProviderResponse>> getProviderById(@PathVariable Long id) {
        return ApiResponse.success(providerService.getProviderById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new shipping provider (admin only)")
    public ResponseEntity<ApiResponse<ShippingProviderResponse>> createProvider(
            @Valid @RequestBody ShippingProviderRequest request) {
        ShippingProviderResponse response = providerService.createProvider(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), response, "Shipping provider created successfully");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a shipping provider (admin only)")
    public ResponseEntity<ApiResponse<ShippingProviderResponse>> updateProvider(
            @PathVariable Long id,
            @Valid @RequestBody ShippingProviderRequest request) {
        return ApiResponse.success(providerService.updateProvider(id, request),
            "Shipping provider updated successfully");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Soft-delete a shipping provider (set inactive)")
    public ResponseEntity<ApiResponse<Void>> deleteProvider(@PathVariable Long id) {
        providerService.deleteProvider(id);
        return ApiResponse.success(null, "Shipping provider deactivated successfully");
    }
}
