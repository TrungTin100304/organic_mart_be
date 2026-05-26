package com.bryan.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final String message;
    private final T data;

    private ApiResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return success(HttpStatus.OK.value(), data, "Success");
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(int status, T data) {
        return success(status, data, "Success");
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return success(HttpStatus.OK.value(), data, message);
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(int status, T data, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(status, message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(int status, String message) {
        return ResponseEntity.status(status).body(new ApiResponse<>(status, message, null));
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}

