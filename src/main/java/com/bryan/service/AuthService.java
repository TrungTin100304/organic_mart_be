package com.bryan.service;

import com.bryan.dto.request.ForgotPasswordRequest;
import com.bryan.dto.request.LoginRequest;
import com.bryan.dto.request.RefreshTokenRequest;
import com.bryan.dto.request.ResetPasswordRequest;
import com.bryan.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse signup(com.bryan.dto.request.SignupRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(String refreshToken);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}
