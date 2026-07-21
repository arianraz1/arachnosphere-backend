package com.tarantulacommunity.tarantulasocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testTokenReusedExceptionConstructor() {
        GlobalExceptionHandler.TokenReusedException exception =
                new GlobalExceptionHandler.TokenReusedException();

        assertEquals("Refresh token reuse detected — all sessions invalidated", exception.getMessage());
        assertTrue(exception instanceof GlobalExceptionHandler.AuthException);
    }

    @Test
    void testHandleAuthWithInvalidRefreshTokenException() {
        GlobalExceptionHandler.InvalidRefreshTokenException exception =
                new GlobalExceptionHandler.InvalidRefreshTokenException("Token is malformed");

        ResponseEntity<ApiError> response = handler.handleAuth(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token is malformed", response.getBody().message());
        assertEquals("AUTH_ERROR", response.getBody().code());
    }

    @Test
    void testHandleAuthWithTokenReusedException() {
        GlobalExceptionHandler.TokenReusedException exception =
                new GlobalExceptionHandler.TokenReusedException();

        ResponseEntity<ApiError> response = handler.handleAuth(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Refresh token reuse detected — all sessions invalidated", response.getBody().message());
        assertEquals("AUTH_ERROR", response.getBody().code());
    }

    @Test
    void testCollectionEntryNotFoundException() {
        GlobalExceptionHandler.CollectionEntryNotFoundException exception =
                new GlobalExceptionHandler.CollectionEntryNotFoundException(1L);

        assertEquals("Collection entry not found with id: 1", exception.getMessage());
    }

    @Test
    void testEmailAlreadyTakenException() {
        GlobalExceptionHandler.EmailAlreadyTakenException exception =
                new GlobalExceptionHandler.EmailAlreadyTakenException("test@example.com");

        assertEquals("Email already taken: test@example.com", exception.getMessage());
    }

    @Test
    void testInvalidMessageException() {
        GlobalExceptionHandler.InvalidMessageException exception =
                new GlobalExceptionHandler.InvalidMessageException("Invalid message content");

        assertEquals("Invalid message content", exception.getMessage());
    }

    @Test
    void testMessageNotFoundException() {
        GlobalExceptionHandler.MessageNotFoundException exception =
                new GlobalExceptionHandler.MessageNotFoundException(1L);

        assertEquals("Message not found with id: 1", exception.getMessage());
    }

    @Test
    void testSpeciesNotFoundException() {
        GlobalExceptionHandler.SpeciesNotFoundException exception =
                new GlobalExceptionHandler.SpeciesNotFoundException(1L);

        assertEquals("Species not found with id: 1", exception.getMessage());
    }

    @Test
    void testUnauthorizedException() {
        GlobalExceptionHandler.UnauthorizedException exception =
                new GlobalExceptionHandler.UnauthorizedException("Not authorized");

        assertEquals("Not authorized", exception.getMessage());
    }

    @Test
    void testUsernameAlreadyTakenException() {
        GlobalExceptionHandler.UsernameAlreadyTakenException exception =
                new GlobalExceptionHandler.UsernameAlreadyTakenException("testuser");

        assertEquals("Username already taken: testuser", exception.getMessage());
    }

    @Test
    void testUserNotFoundException() {
        GlobalExceptionHandler.UserNotFoundException exception =
                new GlobalExceptionHandler.UserNotFoundException(1L);

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void testApiErrorRecord() {
        ApiError error = new ApiError("Test error", "TEST_CODE");

        assertEquals("Test error", error.message());
        assertEquals("TEST_CODE", error.code());
        assertNull(error.fieldErrors());
    }

    @Test
    void testApiErrorRecordWithFieldErrors() {
        Map<String, String> fieldErrors = Map.of("email", "must not be blank");
        ApiError error = new ApiError("Validation failed", "VALIDATION_FAILED", fieldErrors);

        assertEquals("Validation failed", error.message());
        assertEquals("VALIDATION_FAILED", error.code());
        assertEquals(fieldErrors, error.fieldErrors());
    }

    @Test
    void testHandleUserNotFound() {
        GlobalExceptionHandler.UserNotFoundException exception =
                new GlobalExceptionHandler.UserNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleUserNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: 1", response.getBody().message());
        assertEquals("USER_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleSpeciesNotFound() {
        GlobalExceptionHandler.SpeciesNotFoundException exception =
                new GlobalExceptionHandler.SpeciesNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleSpeciesNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Species not found with id: 1", response.getBody().message());
        assertEquals("SPECIES_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleMessageNotFound() {
        GlobalExceptionHandler.MessageNotFoundException exception =
                new GlobalExceptionHandler.MessageNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleMessageNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Message not found with id: 1", response.getBody().message());
        assertEquals("MESSAGE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleCollectionEntryNotFound() {
        GlobalExceptionHandler.CollectionEntryNotFoundException exception =
                new GlobalExceptionHandler.CollectionEntryNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleCollectionEntryNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Collection entry not found with id: 1", response.getBody().message());
        assertEquals("COLLECTION_ENTRY_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleEmailTaken() {
        GlobalExceptionHandler.EmailAlreadyTakenException exception =
                new GlobalExceptionHandler.EmailAlreadyTakenException("test@example.com");

        ResponseEntity<ApiError> response = handler.handleEmailTaken(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already taken: test@example.com", response.getBody().message());
        assertEquals("EMAIL_ALREADY_TAKEN", response.getBody().code());
    }

    @Test
    void testHandleUsernameTaken() {
        GlobalExceptionHandler.UsernameAlreadyTakenException exception =
                new GlobalExceptionHandler.UsernameAlreadyTakenException("testuser");

        ResponseEntity<ApiError> response = handler.handleUsernameTaken(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already taken: testuser", response.getBody().message());
        assertEquals("USERNAME_ALREADY_TAKEN", response.getBody().code());
    }

    @Test
    void testHandleInvalidMessage() {
        GlobalExceptionHandler.InvalidMessageException exception =
                new GlobalExceptionHandler.InvalidMessageException("Invalid message");

        ResponseEntity<ApiError> response = handler.handleInvalidMessage(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid message", response.getBody().message());
        assertEquals("INVALID_MESSAGE", response.getBody().code());
    }

    @Test
    void testHandleUnauthorized() {
        GlobalExceptionHandler.UnauthorizedException exception =
                new GlobalExceptionHandler.UnauthorizedException("Not authorized");

        ResponseEntity<ApiError> response = handler.handleUnauthorized(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authorized", response.getBody().message());
        assertEquals("UNAUTHORIZED", response.getBody().code());
    }

    @Test
    void testHandleBadCredentials() {
        ResponseEntity<ApiError> response = handler.handleBadCredentials();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().message());
        assertEquals("BAD_CREDENTIALS", response.getBody().code());
    }

    @Test
    void testHandleAccessDenied() {
        ResponseEntity<ApiError> response = handler.handleAccessDenied();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().message());
        assertEquals("ACCESS_DENIED", response.getBody().code());
    }

    @Test
    void testHandleMethodNotAllowed() {
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<ApiError> response = handler.handleMethodNotAllowed(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("HTTP method not supported for this endpoint", response.getBody().message());
        assertEquals("METHOD_NOT_ALLOWED", response.getBody().code());
    }

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
                null, null, "testParam", null, null
        );

        ResponseEntity<ApiError> response = handler.handleTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Invalid parameter format"));
        assertEquals("TYPE_MISMATCH", response.getBody().code());
    }

    @Test
    void testHandleMessageNotReadable() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Bad JSON", null);

        ResponseEntity<ApiError> response = handler.handleMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed or missing request body", response.getBody().message());
        assertEquals("MALFORMED_REQUEST", response.getBody().code());
    }

    @Test
    void testThreadNotFoundException() {
        GlobalExceptionHandler.ThreadNotFoundException exception =
                new GlobalExceptionHandler.ThreadNotFoundException(1L);

        assertEquals("Thread not found: 1", exception.getMessage());
    }

    @Test
    void testInvalidTokenException() {
        GlobalExceptionHandler.InvalidTokenException exception =
                new GlobalExceptionHandler.InvalidTokenException("Invalid token");

        assertEquals("Invalid token", exception.getMessage());
    }

    @Test
    void testEmailNotVerifiedException() {
        GlobalExceptionHandler.EmailNotVerifiedException exception =
                new GlobalExceptionHandler.EmailNotVerifiedException("Email not verified");

        assertEquals("Email not verified", exception.getMessage());
    }

    @Test
    void testTooManyRequestsException() {
        GlobalExceptionHandler.TooManyRequestsException exception =
                new GlobalExceptionHandler.TooManyRequestsException("Too many requests");

        assertEquals("Too many requests", exception.getMessage());
    }

    @Test
    void testRateLimitExceededException() {
        GlobalExceptionHandler.RateLimitExceededException exception =
                new GlobalExceptionHandler.RateLimitExceededException("Rate limit exceeded", 60);

        assertEquals("Rate limit exceeded", exception.getMessage());
        assertEquals(60, exception.getRetryAfterSeconds());
    }

    @Test
    void testHandleThreadNotFound() {
        GlobalExceptionHandler.ThreadNotFoundException exception =
                new GlobalExceptionHandler.ThreadNotFoundException(1L);

        ResponseEntity<ApiError> response = handler.handleThreadNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Thread not found: 1", response.getBody().message());
        assertEquals("THREAD_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleInvalidToken() {
        GlobalExceptionHandler.InvalidTokenException exception =
                new GlobalExceptionHandler.InvalidTokenException("Invalid token");

        ResponseEntity<ApiError> response = handler.handleInvalidToken(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().message());
        assertEquals("INVALID_TOKEN", response.getBody().code());
    }

    @Test
    void testHandleEmailNotVerified() {
        GlobalExceptionHandler.EmailNotVerifiedException exception =
                new GlobalExceptionHandler.EmailNotVerifiedException("Email not verified");

        ResponseEntity<ApiError> response = handler.handleEmailNotVerified(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Email not verified", response.getBody().message());
        assertEquals("EMAIL_NOT_VERIFIED", response.getBody().code());
    }

    @Test
    void testHandleTooManyRequests() {
        GlobalExceptionHandler.TooManyRequestsException exception =
                new GlobalExceptionHandler.TooManyRequestsException("Too many requests");

        ResponseEntity<ApiError> response = handler.handleTooManyRequests(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests", response.getBody().message());
        assertEquals("TOO_MANY_REQUESTS", response.getBody().code());
    }

    @Test
    void testHandleRateLimit() {
        GlobalExceptionHandler.RateLimitExceededException exception =
                new GlobalExceptionHandler.RateLimitExceededException("Rate limit exceeded", 60);

        ResponseEntity<ApiError> response = handler.handleRateLimit(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Rate limit exceeded", response.getBody().message());
        assertEquals("RATE_LIMIT_EXCEEDED", response.getBody().code());
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void testHandleNoResourceFound() {
        org.springframework.web.servlet.resource.NoResourceFoundException exception =
                new org.springframework.web.servlet.resource.NoResourceFoundException(
                        org.springframework.http.HttpMethod.GET,
                        "/invalid",
                        "static/invalid"
                );

        ResponseEntity<ApiError> response = handler.handleNoResourceFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The requested resource was not found", response.getBody().message());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleNoHandlerFound() {
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "GET",
                "/unknown-endpoint",
                org.springframework.http.HttpHeaders.EMPTY
        );

        ResponseEntity<ApiError> response = handler.handleNoHandlerFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The requested endpoint does not exist", response.getBody().message());
        assertEquals("ENDPOINT_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleMissingParam() {
        org.springframework.web.bind.MissingServletRequestParameterException exception =
                new org.springframework.web.bind.MissingServletRequestParameterException("param", "String");

        ResponseEntity<ApiError> response = handler.handleMissingParam(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required parameter: param", response.getBody().message());
        assertEquals("MISSING_PARAMETER", response.getBody().code());
    }

    @Test
    void testHandleConstraintViolation() {
        jakarta.validation.ConstraintViolationException exception =
                new jakarta.validation.ConstraintViolationException("Validation failed", java.util.Set.of());

        ResponseEntity<ApiError> response = handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().message());
        assertEquals("VALIDATION_FAILED", response.getBody().code());
        assertTrue(response.getBody().fieldErrors().isEmpty());
    }

    @Test
    void testHandleMissingHeader() {
        org.springframework.web.bind.MissingRequestHeaderException exception =
                new org.springframework.web.bind.MissingRequestHeaderException("Authorization", null);

        ResponseEntity<ApiError> response = handler.handleMissingHeader(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required header: Authorization", response.getBody().message());
        assertEquals("MISSING_HEADER", response.getBody().code());
    }

    @Test
    void testHandleImageNotFound() {
        GlobalExceptionHandler.ImageNotFoundException exception =
                new GlobalExceptionHandler.ImageNotFoundException("Image not found");

        ResponseEntity<ApiError> response = handler.handleImageNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Image not found", response.getBody().message());
        assertEquals("IMAGE_NOT_FOUND", response.getBody().code());
    }

    @Test
    void testHandleImageLimitExceeded() {
        GlobalExceptionHandler.ImageLimitExceededException exception =
                new GlobalExceptionHandler.ImageLimitExceededException("Limit exceeded");

        ResponseEntity<ApiError> response = handler.handleImageLimitExceeded(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Limit exceeded", response.getBody().message());
        assertEquals("IMAGE_LIMIT_EXCEEDED", response.getBody().code());
    }

    @Test
    void testHandleImageUploadFailure() {
        GlobalExceptionHandler.ImageUploadException exception =
                new GlobalExceptionHandler.ImageUploadException("Upload failed", new RuntimeException());

        ResponseEntity<ApiError> response = handler.handleImageUploadFailure(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Image upload failed", response.getBody().message());
        assertEquals("IMAGE_UPLOAD_FAILED", response.getBody().code());
    }

    @Test
    void handleIllegalArgument_returnsBadRequestWithMessage() {
        IllegalArgumentException ex = new IllegalArgumentException("File is empty");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File is empty", response.getBody().message());
        assertEquals("ILLEGAL_ARGUMENT", response.getBody().code());
    }

    @Test
    void handleIllegalArgument_preservesExactMessage_forFileSizeError() {
        IllegalArgumentException ex = new IllegalArgumentException("File size exceeds maximum limit of 10MB");

        ResponseEntity<ApiError> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File size exceeds maximum limit of 10MB", response.getBody().message());
        assertEquals("ILLEGAL_ARGUMENT", response.getBody().code());
    }
}