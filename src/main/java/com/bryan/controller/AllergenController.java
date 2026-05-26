package com.bryan.controller;

import com.bryan.dto.request.AllergenRequest;
import com.bryan.dto.response.AllergenResponse;
import com.bryan.dto.response.ApiResponse;
import com.bryan.entity.Allergen;
import com.bryan.mapper.AllergenMapper;
import com.bryan.service.AllergenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/allergens")
@RequiredArgsConstructor
public class AllergenController {

    private final AllergenService allergenService;
    private final AllergenMapper allergenMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AllergenResponse>>> getAllAllergens() {
        List<AllergenResponse> responses = allergenService.getAllAllergens().stream()
                .map(allergenMapper::toResponse)
                .collect(Collectors.toList());
        return ApiResponse.success(responses);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AllergenResponse>> createAllergen(@RequestBody AllergenRequest request) {
        Allergen createdAllergen = allergenService.createAllergen(request.name());
        return ApiResponse.success(201, allergenMapper.toResponse(createdAllergen));
    }
}

