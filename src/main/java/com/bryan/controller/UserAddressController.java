package com.bryan.controller;

import com.bryan.dto.request.UserAddressRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.UserAddressResponse;
import com.bryan.service.UserAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final UserAddressService userAddressService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")

    public ResponseEntity<ApiResponse<List<UserAddressResponse>>> getAllMyAddresses() {
        return ApiResponse.success(userAddressService.getAllMyAddresses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAddressResponse>> getMyAddressById(@PathVariable Long id) {
        return ApiResponse.success(userAddressService.getMyAddressById(id));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAddressResponse>> createMyAddress(@Valid @RequestBody UserAddressRequest request) {
        return ApiResponse.success(201, userAddressService.createMyAddress(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserAddressResponse>> updateMyAddress(@PathVariable Long id, @Valid @RequestBody UserAddressRequest request) {
        return ApiResponse.success(userAddressService.updateMyAddress(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteMyAddress(@PathVariable Long id) {
        userAddressService.deleteMyAddress(id);
        return ApiResponse.success(null, "User address deleted successfully");
    }
}
