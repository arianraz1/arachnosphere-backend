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

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

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

    // ---- uploadImage compression ----
    // These use real, decodable images (not just magic-byte stubs) since
    // Thumbnailator needs to actually decode the image to compress it.
    // The magic-byte-only fixtures above are undecodable, so compression
    // silently no-ops and falls back to the original bytes for those tests
    // -- which is intentional and already covered by the assertions above.

    @Test
    void uploadImage_compressesLargeJpeg_reducesSizeAndCapsDimensions() throws IOException {
        byte[] largeJpeg = realJpegBytes(3000, 3000);
        MultipartFile file = new MockMultipartFile("file", "big.jpg", "image/jpeg", largeJpeg);

        s3StorageService.uploadImage(file, "profile-pictures");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        assertEquals("image/jpeg", captor.getValue().contentType());
        assertTrue(captor.getValue().contentLength() < largeJpeg.length,
                "compressed/resized image should be smaller than the original 3000x3000 source");
    }

    @Test
    void uploadImage_convertsNoisyPngToJpeg_whenCompressionShrinksIt() throws IOException {
        // Random-noise PNGs compress poorly under PNG's lossless filter but
        // shrink significantly under JPEG's lossy quantization -- this
        // deterministically exercises the "compressed is smaller" branch.
        byte[] noisyPng = realNoisyPngBytes(800, 800);
        MultipartFile file = new MockMultipartFile("file", "pic.png", "image/png", noisyPng);

        String key = s3StorageService.uploadImage(file, "favorite-images");

        assertTrue(key.endsWith(".jpg"), "compression should switch the stored extension to .jpg");

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("image/jpeg", captor.getValue().contentType());
        assertTrue(captor.getValue().contentLength() < noisyPng.length);
    }

    @Test
    void uploadImage_skipsCompression_forGif_evenWithRealDecodableImage() throws IOException {
        byte[] realGif = realGifBytes(200, 200);
        MultipartFile file = new MockMultipartFile("file", "pic.gif", "image/gif", realGif);

        String key = s3StorageService.uploadImage(file, "favorite-images");

        assertTrue(key.endsWith(".gif"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("image/gif", captor.getValue().contentType());
        assertEquals(realGif.length, captor.getValue().contentLength());
    }

    @Test
    void uploadImage_fallsBackToOriginal_whenCompressedResultIsNotSmaller() throws IOException {
        // A tiny 2x2 solid-color image: JPEG's per-block overhead makes the
        // "compressed" output larger than the trivial original, so the
        // original bytes/content-type/extension should be kept as-is.
        byte[] tinyJpeg = realJpegBytes(2, 2);
        MultipartFile file = new MockMultipartFile("file", "tiny.jpg", "image/jpeg", tinyJpeg);

        String key = s3StorageService.uploadImage(file, "profile-pictures");

        assertTrue(key.endsWith(".jpg"));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("image/jpeg", captor.getValue().contentType());
        assertEquals(tinyJpeg.length, captor.getValue().contentLength(),
                "original should be kept when 'compression' wouldn't shrink it");
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
        s3Config.setPublicUrlIncludesBucket(true);

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("http://localstack:4566/test-bucket/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_prefersPublicEndpoint_whenBothEndpointsSet() {
        s3Config.setEndpointUrl("http://localstack:4566");
        s3Config.setPublicEndpointUrl("http://localhost:4566");
        s3Config.setPublicUrlIncludesBucket(true);

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("http://localhost:4566/test-bucket/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_ignoresBlankPublicEndpoint_fallsBackToEndpointUrl() {
        s3Config.setEndpointUrl("http://localstack:4566");
        s3Config.setPublicEndpointUrl("   ");
        s3Config.setPublicUrlIncludesBucket(true);

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
    void getImageUrl_r2Style_omitsBucketFromPath_whenFlagIsFalse() {
        s3Config.setEndpointUrl("https://account-id.r2.cloudflarestorage.com");
        s3Config.setPublicEndpointUrl("https://pub-xxxx.r2.dev");
        s3Config.setPublicUrlIncludesBucket(false);

        String url = s3StorageService.getImageUrl("profile-pictures/abc.jpg");

        assertEquals("https://pub-xxxx.r2.dev/profile-pictures/abc.jpg", url);
    }

    @Test
    void getImageUrl_returnsNullWhenKeyIsNull() {
        assertNull(s3StorageService.getImageUrl(null));
    }

    @Test
    void getImageUrl_returnsNullWhenKeyIsBlank() {
        assertNull(s3StorageService.getImageUrl("   "));
    }

    // ---- test image fixtures ----

    private static byte[] realJpegBytes(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.RED);
        g.fillRect(0, 0, width, height);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        return baos.toByteArray();
    }

    private static byte[] realNoisyPngBytes(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Random rnd = new Random(42); // fixed seed for deterministic test results
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                img.setRGB(x, y, rnd.nextInt(0xFFFFFF));
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    private static byte[] realGifBytes(int width, int height) throws IOException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.BLUE);
        g.fillRect(0, 0, width, height);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "gif", baos);
        return baos.toByteArray();
    }
}