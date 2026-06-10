package com.bryan.service.impl;

import com.bryan.config.CloudinaryProperties;
import com.bryan.exception.BadRequestException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileUploadServiceImplTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    private FileUploadServiceImpl service;

    @BeforeEach
    void setUp() {
        CloudinaryProperties properties = new CloudinaryProperties("demo-cloud", "api-key", "api-secret");
        service = new FileUploadServiceImpl(cloudinary, properties);
    }

    @Test
    void shouldUploadImageToRequestedFolderAndReturnSecureUrl() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "product.jpg", "image/jpeg", jpegBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/product.jpg"));

        String url = service.uploadFile(file, "organic-mart/products");

        assertEquals("https://res.cloudinary.com/demo/product.jpg", url);
        @SuppressWarnings("rawtypes")
        ArgumentCaptor<Map> options = ArgumentCaptor.forClass(Map.class);
        verify(uploader).upload(any(byte[].class), options.capture());
        assertEquals("image", options.getValue().get("resource_type"));
        assertEquals("organic-mart/products", options.getValue().get("folder"));
    }

    @Test
    void shouldRejectUnsupportedContentType() {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "script.svg", "image/svg+xml", "<svg />".getBytes());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.uploadFile(file, "organic-mart/products"));

        assertEquals("Chỉ chấp nhận ảnh JPEG, PNG hoặc WEBP.", exception.getMessage());
    }

    @Test
    void shouldRejectImageLargerThanFiveMegabytes() {
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", new byte[(5 * 1024 * 1024) + 1]);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.uploadFile(file, "organic-mart/avatars"));

        assertEquals("Ảnh không được vượt quá 5MB.", exception.getMessage());
    }

    @Test
    void shouldRejectUploadWhenCloudinaryIsNotConfigured() {
        service = new FileUploadServiceImpl(cloudinary, new CloudinaryProperties("", "", ""));
        MockMultipartFile file = new MockMultipartFile(
                "avatar", "avatar.webp", "image/webp", webpBytes());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.uploadFile(file, "organic-mart/avatars"));

        assertEquals("Dịch vụ tải ảnh chưa được cấu hình.", exception.getMessage());
    }

    @Test
    void shouldHideCloudinaryFailureDetailsFromClient() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "imageFile", "product.jpg", "image/jpeg", jpegBytes());
        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), any(Map.class)))
                .thenThrow(new RuntimeException("provider response contains sensitive details"));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.uploadFile(file, "organic-mart/products"));

        assertEquals("Không thể tải ảnh lên. Vui lòng thử lại.", exception.getMessage());
    }

    private byte[] jpegBytes() {
        return new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0x01};
    }

    private byte[] webpBytes() {
        return new byte[] {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};
    }
}
