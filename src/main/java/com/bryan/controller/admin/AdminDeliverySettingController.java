package com.bryan.controller.admin;

import com.bryan.dto.request.DeliverySettingRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.entity.DeliveryMethod;
import com.bryan.service.DeliverySettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/delivery-settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDeliverySettingController {

    private final DeliverySettingService deliverySettingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DeliverySettingService.Setting>>> getSettings() {
        return ApiResponse.success(deliverySettingService.getAllSettings());
    }

    @PutMapping("/{deliveryMethod}")
    public ResponseEntity<ApiResponse<DeliverySettingService.Setting>> updateSetting(
            @PathVariable DeliveryMethod deliveryMethod,
            @Valid @RequestBody DeliverySettingRequest request) {
        return ApiResponse.success(deliverySettingService.updateSetting(deliveryMethod, request));
    }
}
