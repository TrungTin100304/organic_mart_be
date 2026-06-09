package com.bryan.controller;

import com.bryan.dto.request.FarmRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.FarmResponse;
import com.bryan.service.FarmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/farms")
@RequiredArgsConstructor
public class FarmController {

    private final FarmService farmService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FarmResponse>>> getAllFarms() {
        return ApiResponse.success(farmService.getAllFarms());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FarmResponse>> getFarmById(@PathVariable Long id) {
        return ApiResponse.success(farmService.getFarmById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FarmResponse>> createFarm(@Valid @RequestBody FarmRequest request) {
        return ApiResponse.success(201, farmService.createFarm(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FarmResponse>> updateFarm(@PathVariable Long id, @Valid @RequestBody FarmRequest request) {
        return ApiResponse.success(farmService.updateFarm(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFarm(@PathVariable Long id) {
        farmService.deleteFarm(id);
        return ApiResponse.success(null, "Farm deleted successfully");
    }
}
