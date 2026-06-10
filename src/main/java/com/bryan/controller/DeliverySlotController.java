package com.bryan.controller;

import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.AvailableSlotResponse;
import com.bryan.service.DeliverySlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/delivery-slots")
@RequiredArgsConstructor
public class DeliverySlotController {

    private final DeliverySlotService slotService;

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<AvailableSlotResponse>>> getAvailableSlots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.success(slotService.getAvailableSlots(date));
    }
}
