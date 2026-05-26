package com.bryan.exception;

import com.bryan.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.bryan.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(BadRequestException ex) {
        int statusCode = 400; // Mặc định là 400 Bad Request

        // Tùy chỉnh status theo logic như bạn yêu cầu trước đó
        if (ex.getMessage().contains("already in use")) {
            statusCode = 409; // Conflict
        } else if (ex.getMessage().contains("Invalid email or password")) {
            statusCode = 401; // Unauthorized
        }

        return ApiResponse.error(statusCode, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Dữ liệu không hợp lệ";

        return ApiResponse.error(400, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(Exception ex) {
        return ApiResponse.error(500, "Đã xảy ra lỗi trên hệ thống: " + ex.getMessage());
    }
}
