package com.bryan.controller;

import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.DeliveryFeeResponse;
import com.bryan.entity.DeliveryMethod;
import com.bryan.service.DeliverySettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final ApplicationContext ctx;

    private DeliverySettingService getService() {
        return ctx.getBean(DeliverySettingService.class);
    }

    @GetMapping("/fees")
    public ResponseEntity<ApiResponse<List<DeliveryFeeResponse>>> getDeliveryFees() {
        List<DeliveryFeeResponse> fees = getService().getAllSettings().stream()
                .filter(setting -> setting.enabled())
                .sorted(Comparator.comparing(
                        setting -> setting.displayOrder() != null ? setting.displayOrder() : 0))
                .map(setting -> new DeliveryFeeResponse(
                        DeliveryMethod.valueOf(setting.deliveryType()),
                        setting.fee(),
                        setting.freeShippingThreshold(),
                        setting.estimatedMinutes() != null ? setting.estimatedMinutes() : 0,
                        "SCHEDULED".equals(setting.deliveryType())
                                ? "Theo khung gio ban chon"
                                : (setting.estimatedMinutes() != null ? setting.estimatedMinutes() : 0) + " phut"
                ))
                .toList();
        return ApiResponse.success(fees);
    }
}
