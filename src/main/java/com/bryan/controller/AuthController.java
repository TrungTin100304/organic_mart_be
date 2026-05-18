package com.bryan.controller;

import com.bryan.dto.request.ForgotPasswordRequest;
import com.bryan.dto.request.LoginRequest;
import com.bryan.dto.request.RefreshTokenRequest;
import com.bryan.dto.request.ResetPasswordRequest;
import com.bryan.dto.response.ApiResponse;
import com.bryan.dto.response.AuthResponse;
import com.bryan.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @PreAuthorize("permitAll()")
    public ApiResponse<AuthResponse> signup(@Valid @RequestBody com.bryan.dto.request.SignupRequest request) {
        return ApiResponse.success(201, null, "User registered successfully");
    }

    @PostMapping("/login")
    @PreAuthorize("permitAll()")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request), "Login successful");
    }

    @PostMapping("/refresh")
    @PreAuthorize("permitAll()")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request), "Token refreshed successfully");
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.refreshToken());
        return ApiResponse.success(200, "Logged out successfully");
    }


    @PostMapping("/forgot-password")
    @PreAuthorize("permitAll()")
    public ApiResponse<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ApiResponse.success(200, "Password reset email sent");
    }

    @PostMapping("/reset-password")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success(200, "Password reset successfully");
    }
}
