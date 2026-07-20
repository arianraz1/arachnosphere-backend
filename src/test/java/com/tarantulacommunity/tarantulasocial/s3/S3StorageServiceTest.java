package com.tarantulacommunity.tarantulasocial.s3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    private S3Config s3Config;
    private S3StorageService s3StorageService;

    private static final byte[] JPEG_BYTES = {(byte) 0xFF, (byte) 0xD8, 0x01, 0x02, 0x03};
    private static final byte[] PNG_BYTES = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x01, 0x02};
    private static final byte[] GIF_BYTES = {0x47, 0x49, 0x46, 0x38, 0x01, 0x02};
    private static final byte[] WEBP_BYTES = {
            0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50
    };
    private static final byte[] INVALID_BYTES = {0x00, 0x01, 0x02, 0x03};

    @BeforeEach
    void setUp() {
        s3Config = new S3Config();
        s3Config.setBucket("test-bucket");
        s3Config.setRegion("us-east-1");
        s3StorageService = new S3StorageService(s3Client, s3Config);
    }

    // ---- uploadImage ----

    @Test
    void uploadImage_success_returnsKeyWithFolderAndExtension() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "tarantula.jpg", "image/jpeg", JPEG_BYTES);

        String key = s3StorageService.uploadImage(file, "profile-pictures");

        assertNotNull(key);
        assertTrue(key.startsWith("profile-pictures/"));
        assertTrue(key.endsWith(".jpg"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals(key, captor.getValue().key());
        assertEquals("image/jpeg", captor.getValue().contentType());
    }

    @Test
    void uploadImage_handlesFilenameWithNoExtension() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "tarantula", "image/jpeg", JPEG_BYTES);

        String key = s3StorageService.uploadImage(file, "profile-pictures");

        assertTrue(key.startsWith("profile-pictures/"));
        assertFalse(key.contains("."));
    }

    @Test
    void uploadImage_handlesNullOriginalFilename() throws IOException {
        MultipartFile file = new MockMultipartFile("file", null, "image/jpeg", JPEG_BYTES);

        String key = s3StorageService.uploadImage(file, "profile-pictures");

        assertTrue(key.startsWith("profile-pictures/"));
    }

    @Test
    void uploadImage_acceptsPng() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", PNG_BYTES);

        assertDoesNotThrow(() -> s3StorageService.uploadImage(file, "favorite-images"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_acceptsGif() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "pic.gif", "image/gif", GIF_BYTES);

        assertDoesNotThrow(() -> s3StorageService.uploadImage(file, "favorite-images"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_acceptsWebp() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "pic.webp", "image/webp", WEBP_BYTES);

        assertDoesNotThrow(() -> s3StorageService.uploadImage(file, "favorite-images"));
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    @Test
    void uploadImage_throwsWhenFileEmpty() {
        MultipartFile file = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        assertEquals("File is empty", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_throwsWhenFileTooLarge() {
        byte[] oversized = new byte[11 * 1024 * 1024]; // 11MB
        System.arraycopy(JPEG_BYTES, 0, oversized, 0, JPEG_BYTES.length);
        MultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", oversized);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        assertEquals("File size exceeds maximum limit of 10MB", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_throwsWhenContentTypeNotAllowed() {
        MultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", JPEG_BYTES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        assertEquals("Invalid file type. Only JPEG, PNG, GIF, and WebP are allowed", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_throwsWhenContentTypeIsNull() {
        MultipartFile file = new MockMultipartFile("file", "pic.jpg", null, JPEG_BYTES);

        assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_throwsWhenContentTypeAllowedButBytesAreNotAnImage() {
        // spoofed content-type: claims to be a jpeg but magic bytes don't match
        MultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", INVALID_BYTES);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        assertEquals("File content is not a valid image", ex.getMessage());
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_throwsWhenBytesTooShortToIdentify() {
        MultipartFile file = new MockMultipartFile("file", "tiny.jpg", "image/jpeg", new byte[]{0x01});

        assertThrows(IllegalArgumentException.class,
                () -> s3StorageService.uploadImage(file, "profile-pictures"));
        verifyNoInteractions(s3Client);
    }

    @Test
    void uploadImage_generatesUniqueKeysForRepeatedUploads() throws IOException {
        MultipartFile file1 = new MockMultipartFile("file", "a.jpg", "image/jpeg", JPEG_BYTES);
        MultipartFile file2 = new MockMultipartFile("file", "b.jpg", "image/jpeg", JPEG_BYTES);

        String key1 = s3StorageService.uploadImage(file1, "profile-pictures");
        String key2 = s3StorageService.uploadImage(file2, "profile-pictures");

        assertNotEquals(key1, key2);
    }

    // ---- deleteImage ----

    @Test
    void deleteImage_callsS3ClientWithCorrectBucketAndKey() {
        s3StorageService.deleteImage("profile-pictures/abc.jpg");

        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertEquals("profile-pictures/abc.jpg", captor.getValue().key());
    }

    @Test
    void deleteImage_doesNothingWhenKeyIsNull() {
        s3StorageService.deleteImage(null);
        verifyNoInteractions(s3Client);
    }

    @Test
    void deleteImage_doesNothingWhenKeyIsBlank() {
        s3StorageService.deleteImage("   ");
        verifyNoInteractions(s3Client);
    }

    @Test
    void deleteImage_wrapsS3ExceptionAsRuntimeException() {
        doThrow(RuntimeException.class).when(s3Client).deleteObject(any(DeleteObjectRequest.class));

        assertThrows(RuntimeException.class,
                () -> s3StorageService.deleteImage("profile-pictures/abc.jpg"));
    }

    // ---- getImageUrl ----

    @Test
    void getImageUrl_returnsFormattedUrl() {
        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_usesCustomEndpoint_whenPublicEndpointNotSet() {
        s3Config.setEndpointUrl("http://localstack:4566");

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("http://localstack:4566/test-bucket/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_prefersPublicEndpoint_whenBothEndpointsSet() {
        s3Config.setEndpointUrl("http://localstack:4566");
        s3Config.setPublicEndpointUrl("http://localhost:4566");

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("http://localhost:4566/test-bucket/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_ignoresBlankPublicEndpoint_fallsBackToEndpointUrl() {
        s3Config.setEndpointUrl("http://localstack:4566");
        s3Config.setPublicEndpointUrl("   ");

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("http://localstack:4566/test-bucket/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_realAwsBranch_ignoresPublicEndpoint_whenNoCustomEndpointSet() {
        // publicEndpointUrl set but endpointUrl is not -> isUsingCustomEndpoint() is false,
        // should still hit the real-AWS branch untouched
        s3Config.setPublicEndpointUrl("http://localhost:4566");

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("https://test-bucket.s3.us-east-1.amazonaws.com/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_returnsNullWhenKeyIsNull() {
        assertNull(s3StorageService.getImageUrl(null));
    }

    @Test
    void getImageUrl_returnsNullWhenKeyIsBlank() {
        assertNull(s3StorageService.getImageUrl("   "));
    }
}
