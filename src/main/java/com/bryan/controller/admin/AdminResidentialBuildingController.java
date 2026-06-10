package com.bryan.controller.admin;

import com.bryan.dto.request.DeliverySlotRequest;
import com.bryan.dto.request.ResidentialBuildingRequest;
import com.bryan.dto.request.StatusRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.DeliverySlotResponse;
import com.bryan.dto.response.ResidentialBuildingResponse;
import com.bryan.service.DeliverySlotService;
import com.bryan.service.ResidentialBuildingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/residential-buildings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminResidentialBuildingController {

    private final ResidentialBuildingService buildingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ResidentialBuildingResponse>>> getAllBuildings() {
        return ApiResponse.success(buildingService.getAllBuildings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ResidentialBuildingResponse>> getBuildingById(@PathVariable Long id) {
        return ApiResponse.success(buildingService.getBuildingById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ResidentialBuildingResponse>> createBuilding(
            @Valid @RequestBody ResidentialBuildingRequest request) {
        return ApiResponse.success(201, buildingService.createBuilding(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ResidentialBuildingResponse>> updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody ResidentialBuildingRequest request) {
        return ApiResponse.success(buildingService.updateBuilding(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<ResidentialBuildingResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request) {
        return ApiResponse.success(buildingService.updateStatus(id, request));
    }
}
