package com.bryan.exception;

import com.bryan.dto.response.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerUploadTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnPayloadTooLargeForOversizedMultipartRequest() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleMaxUploadSizeExceededException(
                new MaxUploadSizeExceededException(5L * 1024 * 1024));

        assertEquals(413, response.getStatusCode().value());
        assertEquals("Ảnh không được vượt quá 5MB.", response.getBody().getMessage());
    }

    @Test
    void shouldReturnBadRequestForMalformedMultipartRequest() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleMultipartException(
                new MultipartException("malformed request"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Dữ liệu tải ảnh không hợp lệ.", response.getBody().getMessage());
    }
}
