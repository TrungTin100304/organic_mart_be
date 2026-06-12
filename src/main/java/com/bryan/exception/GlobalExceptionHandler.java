package com.bryan.exception;

import com.bryan.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice(basePackages = "com.bryan.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        int statusCode = 400;

        if (ex.getMessage().contains("already in use")) {
            statusCode = 409;
        } else if (ex.getMessage().contains("Invalid email or password")) {
            statusCode = 401;
        }

        return ApiResponse.error(statusCode, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ";

        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return ApiResponse.error(413, "Ảnh không được vượt quá 5MB.");
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex) {
        return ApiResponse.error(400, "Dữ liệu tải ảnh không hợp lệ.");
    }

    @ExceptionHandler(AiResponseParseException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiParseException(AiResponseParseException ex) {
        return ApiResponse.error(502, "Không thể tạo thực đơn. Vui lòng thử lại: " + ex.getMessage());
    }

    @ExceptionHandler(AiTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiTimeoutException(AiTimeoutException ex) {
        return ApiResponse.error(504, "Máy chủ AI đang quá tải. Vui lòng thử lại sau vài phút.");
    }

    @ExceptionHandler(AiQuotaExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleAiQuotaExceededException(AiQuotaExceededException ex) {
        return ApiResponse.error(429, ex.getMessage());
    }

    @ExceptionHandler(MealPlanRateLimitException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitException(MealPlanRateLimitException ex) {
        return ApiResponse.error(429, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        return ApiResponse.error(403, "Bạn không có quyền thực hiện thao tác này.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        return ApiResponse.error(401, "Vui lòng đăng nhập để tiếp tục.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        return ApiResponse.error(500, "Đã xảy ra lỗi trên hệ thống: " + ex.getMessage());
    }
}
