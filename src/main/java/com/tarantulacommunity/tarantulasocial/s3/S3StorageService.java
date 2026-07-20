package com.tarantulacommunity.tarantulasocial.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3StorageService {

    private final S3Client s3Client;
    private final S3Config s3Config;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_CONTENT_TYPES = {
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        byte[] content = file.getBytes();
        validateImage(file, content);

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        String uniqueFilename = UUID.randomUUID() + extension;
        String key = folder + "/" + uniqueFilename;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

        log.info("Successfully uploaded image to S3: {}", key);
        return key;
    }

    public void deleteImage(String key) {
        if (key == null || key.isBlank()) {
            log.warn("Attempted to delete image with null or blank key");
            return;
        }

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(s3Config.getBucket())
                .key(key)
                .build();

        try {
            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted image from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete image from S3: {}", key, e);
            throw new RuntimeException("Failed to delete image from S3", e);
        }
    }

    public String getImageUrl(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        if (s3Config.isUsingCustomEndpoint()) {
            String base = (s3Config.getPublicEndpointUrl() != null && !s3Config.getPublicEndpointUrl().isBlank())
                    ? s3Config.getPublicEndpointUrl()
                    : s3Config.getEndpointUrl();

            return String.format("%s/%s/%s",
                    base,
                    s3Config.getBucket(),
                    key);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Config.getBucket(),
                s3Config.getRegion(),
                key);
    }

    private void validateImage(MultipartFile file, byte[] content) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed");
        }
        if (!isImageContent(content)) {
            throw new IllegalArgumentException("File content is not a valid image");
        }
    }

    private boolean isAllowedContentType(String contentType) {
        for (String allowed : ALLOWED_CONTENT_TYPES) {
            if (allowed.equalsIgnoreCase(contentType)) return true;
        }
        return false;
    }

    private boolean isImageContent(byte[] bytes) {
        if (bytes.length < 2) return false;

        if (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8) return true; // JPEG

        if (bytes.length >= 4 &&
                bytes[0] == (byte) 0x89 &&
                bytes[1] == (byte) 0x50 &&
                bytes[2] == (byte) 0x4E &&
                bytes[3] == (byte) 0x47) return true; // PNG

        if (bytes.length >= 4 &&
                bytes[0] == (byte) 0x47 &&
                bytes[1] == (byte) 0x49 &&
                bytes[2] == (byte) 0x46 &&
                bytes[3] == (byte) 0x38) return true; // GIF

        if (bytes.length >= 12 &&
                bytes[0] == (byte) 0x52 &&
                bytes[1] == (byte) 0x49 &&
                bytes[2] == (byte) 0x46 &&
                bytes[3] == (byte) 0x46 &&
                bytes[8] == (byte) 0x57 &&
                bytes[9] == (byte) 0x45 &&
                bytes[10] == (byte) 0x42 &&
                bytes[11] == (byte) 0x50) return true; // WebP

        return false;
    }
}
