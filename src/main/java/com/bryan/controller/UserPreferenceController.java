package com.bryan.controller;

import com.bryan.dto.request.UpdateUserPreferenceRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.UserPreferenceResponse;
import com.bryan.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getCurrentUserPreference() {
        return ApiResponse.success(userPreferenceService.getCurrentUserPreference());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> getPreferenceById(@PathVariable Long id) {
        return ApiResponse.success(userPreferenceService.getPreferenceById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> createOrUpdatePreference(
            @Valid @RequestBody UpdateUserPreferenceRequest request) {
        return ApiResponse.success(201, userPreferenceService.createOrUpdatePreference(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> updatePreference(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPreferenceRequest request) {
        return ApiResponse.success(userPreferenceService.updatePreference(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deletePreference(@PathVariable Long id) {
        userPreferenceService.deletePreference(id);
        return ApiResponse.success(null, "UserPreference deleted successfully");
    }
}
