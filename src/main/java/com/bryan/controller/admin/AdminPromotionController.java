package com.bryan.controller.admin;

import com.bryan.dto.request.AdminPromotionRequest;
import com.bryan.dto.response.AdminPromotionResponse;
import com.bryan.dto.response.ApiResponse;
import com.bryan.service.AdminPromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/promotions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminPromotionController {

    private final AdminPromotionService promotionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminPromotionResponse>>> getAll() {
        return ApiResponse.success(promotionService.getAll());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AdminPromotionResponse>> create(
            @Valid @RequestBody AdminPromotionRequest request) {
        return ApiResponse.success(201, promotionService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPromotionResponse>> update(
            @PathVariable Long id, @Valid @RequestBody AdminPromotionRequest request) {
        return ApiResponse.success(promotionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPromotionResponse>> deactivate(@PathVariable Long id) {
        return ApiResponse.success(promotionService.deactivate(id), "Promotion deactivated successfully");
    }
}
