package com.bryan.service.impl;

import com.bryan.config.CloudinaryProperties;
import com.bryan.exception.BadRequestException;
import com.bryan.service.FileUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp");

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public String uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Vui lòng chọn ảnh để tải lên.");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BadRequestException("Chỉ chấp nhận ảnh JPEG, PNG hoặc WEBP.");
        }
        if (file.getSize() > MAX_IMAGE_SIZE) {
            throw new BadRequestException("Ảnh không được vượt quá 5MB.");
        }
        if (!properties.isConfigured()) {
            throw new BadRequestException("Dịch vụ tải ảnh chưa được cấu hình.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("Could not read uploaded image", e);
            throw new BadRequestException("Không thể đọc file ảnh.");
        }
        if (!isValidImageContent(bytes, file.getContentType())) {
            throw new BadRequestException("Nội dung file không phải ảnh hợp lệ.");
        }

        Map<?, ?> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(bytes, ObjectUtils.asMap(
                    "public_id", UUID.randomUUID().toString(),
                    "folder", folder,
                    "resource_type", "image"
            ));
        } catch (IOException | RuntimeException e) {
            log.error("Cloudinary image upload failed", e);
            throw new BadRequestException("Không thể tải ảnh lên. Vui lòng thử lại.");
        }

        Object secureUrl = uploadResult.get("secure_url");
        if (secureUrl == null || secureUrl.toString().isBlank()) {
            log.error("Cloudinary upload succeeded without secure_url");
            throw new BadRequestException("Không thể tải ảnh lên. Vui lòng thử lại.");
        }
        return secureUrl.toString();
    }

    private boolean isValidImageContent(byte[] bytes, String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> startsWith(bytes, new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff});
            case "image/png" -> startsWith(bytes, new byte[] {
                    (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a});
            case "image/webp" -> startsWith(bytes, new byte[] {0x52, 0x49, 0x46, 0x46})
                    && bytes.length >= 12
                    && Arrays.equals(Arrays.copyOfRange(bytes, 8, 12), new byte[] {0x57, 0x45, 0x42, 0x50});
            default -> false;
        };
    }

    private boolean startsWith(byte[] bytes, byte[] signature) {
        return bytes.length >= signature.length
                && Arrays.equals(Arrays.copyOf(bytes, signature.length), signature);
    }
}

