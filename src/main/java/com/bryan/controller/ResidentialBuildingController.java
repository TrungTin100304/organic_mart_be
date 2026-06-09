package com.bryan.controller;

import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.DeliveryFeeResponse;
import com.bryan.dto.response.ResidentialBuildingResponse;
import com.bryan.service.ResidentialBuildingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/residential-buildings")
@RequiredArgsConstructor
public class ResidentialBuildingController {

    private final ResidentialBuildingService buildingService;

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<ResidentialBuildingResponse>>> getActiveBuildings() {
        return ApiResponse.success(buildingService.getActiveBuildings());
    }
}
