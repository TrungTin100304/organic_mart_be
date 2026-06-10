package com.bryan.controller.admin;

import com.bryan.dto.request.DeliverySlotRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.DeliverySlotResponse;
import com.bryan.service.DeliverySlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/delivery-slots")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeliverySlotController {

    private final DeliverySlotService slotService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliverySlotResponse>>> getAllSlots() {
        return ApiResponse.success(slotService.getAllSlots());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliverySlotResponse>> getSlotById(@PathVariable Long id) {
        return ApiResponse.success(slotService.getSlotById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DeliverySlotResponse>> createSlot(
            @Valid @RequestBody DeliverySlotRequest request) {
        return ApiResponse.success(201, slotService.createSlot(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeliverySlotResponse>> updateSlot(
            @PathVariable Long id,
            @Valid @RequestBody DeliverySlotRequest request) {
        return ApiResponse.success(slotService.updateSlot(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<DeliverySlotResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return ApiResponse.success(slotService.updateStatus(id, request));
    }
}
