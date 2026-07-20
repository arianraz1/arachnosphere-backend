package com.tarantulacommunity.tarantulasocial.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleAuth(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Token is malformed", response.getBody().message());
    }

    @Test
    void testHandleAuthWithTokenReusedException() {
        GlobalExceptionHandler.TokenReusedException exception =
                new GlobalExceptionHandler.TokenReusedException();

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleAuth(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Refresh token reuse detected — all sessions invalidated", response.getBody().message());
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
    void testErrorResponseRecord() {
        GlobalExceptionHandler.ErrorResponse response = new GlobalExceptionHandler.ErrorResponse("Test error");

        assertEquals("Test error", response.message());
    }

    @Test
    void testHandleUserNotFound() {
        GlobalExceptionHandler.UserNotFoundException exception =
            new GlobalExceptionHandler.UserNotFoundException(1L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleUserNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("User not found with id: 1", response.getBody().message());
    }

    @Test
    void testHandleSpeciesNotFound() {
        GlobalExceptionHandler.SpeciesNotFoundException exception =
            new GlobalExceptionHandler.SpeciesNotFoundException(1L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleSpeciesNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Species not found with id: 1", response.getBody().message());
    }

    @Test
    void testHandleMessageNotFound() {
        GlobalExceptionHandler.MessageNotFoundException exception =
            new GlobalExceptionHandler.MessageNotFoundException(1L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleMessageNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Message not found with id: 1", response.getBody().message());
    }

    @Test
    void testHandleCollectionEntryNotFound() {
        GlobalExceptionHandler.CollectionEntryNotFoundException exception =
            new GlobalExceptionHandler.CollectionEntryNotFoundException(1L);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleCollectionEntryNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Collection entry not found with id: 1", response.getBody().message());
    }

    @Test
    void testHandleEmailTaken() {
        GlobalExceptionHandler.EmailAlreadyTakenException exception =
            new GlobalExceptionHandler.EmailAlreadyTakenException("test@example.com");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleEmailTaken(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Email already taken: test@example.com", response.getBody().message());
    }

    @Test
    void testHandleUsernameTaken() {
        GlobalExceptionHandler.UsernameAlreadyTakenException exception =
            new GlobalExceptionHandler.UsernameAlreadyTakenException("testuser");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleUsernameTaken(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Username already taken: testuser", response.getBody().message());
    }

    @Test
    void testHandleInvalidMessage() {
        GlobalExceptionHandler.InvalidMessageException exception =
            new GlobalExceptionHandler.InvalidMessageException("Invalid message");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleInvalidMessage(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid message", response.getBody().message());
    }

    @Test
    void testHandleUnauthorized() {
        GlobalExceptionHandler.UnauthorizedException exception =
            new GlobalExceptionHandler.UnauthorizedException("Not authorized");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleUnauthorized(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Not authorized", response.getBody().message());
    }

    @Test
    void testHandleBadCredentials() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleBadCredentials();

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody().message());
    }

    @Test
    void testHandleAccessDenied() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleAccessDenied();

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().message());
    }

    @Test
    void testHandleMethodNotAllowed() {
        HttpRequestMethodNotSupportedException exception =
            new HttpRequestMethodNotSupportedException("POST");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleMethodNotAllowed(exception);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("HTTP method not supported for this endpoint", response.getBody().message());
    }

    @Test
    void testHandleTypeMismatch() {
        MethodArgumentTypeMismatchException exception = new MethodArgumentTypeMismatchException(
            null, null, "testParam", null, null
        );

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleTypeMismatch(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().message().contains("Invalid parameter format"));
    }

    @Test
    void testHandleMessageNotReadable() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Bad JSON", null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleMessageNotReadable(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Malformed or missing request body", response.getBody().message());
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

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleThreadNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Thread not found: 1", response.getBody().message());
    }

    @Test
    void testHandleInvalidToken() {
        GlobalExceptionHandler.InvalidTokenException exception =
            new GlobalExceptionHandler.InvalidTokenException("Invalid token");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleInvalidToken(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid token", response.getBody().message());
    }

    @Test
    void testHandleEmailNotVerified() {
        GlobalExceptionHandler.EmailNotVerifiedException exception =
            new GlobalExceptionHandler.EmailNotVerifiedException("Email not verified");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleEmailNotVerified(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Email not verified", response.getBody().message());
    }

    @Test
    void testHandleTooManyRequests() {
        GlobalExceptionHandler.TooManyRequestsException exception =
            new GlobalExceptionHandler.TooManyRequestsException("Too many requests");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleTooManyRequests(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Too many requests", response.getBody().message());
    }

    @Test
    void testHandleRateLimit() {
        GlobalExceptionHandler.RateLimitExceededException exception =
            new GlobalExceptionHandler.RateLimitExceededException("Rate limit exceeded", 60);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleRateLimit(exception);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("Rate limit exceeded", response.getBody().message());
        assertEquals("60", response.getHeaders().getFirst("Retry-After"));
    }

    @Test
    void testHandleNoResourceFound() {
        // Correctly passes the HttpMethod enum, the request URI, and the failed resource path
        org.springframework.web.servlet.resource.NoResourceFoundException exception =
                new org.springframework.web.servlet.resource.NoResourceFoundException(
                        org.springframework.http.HttpMethod.GET,
                        "/invalid",
                        "static/invalid"
                );

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleNoResourceFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The requested resource was not found", response.getBody().message());
    }


    @Test
    void testHandleNoHandlerFound() {
        NoHandlerFoundException exception = new NoHandlerFoundException(
                "GET",
                "/unknown-endpoint",
                org.springframework.http.HttpHeaders.EMPTY
        );

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleNoHandlerFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The requested endpoint does not exist", response.getBody().message());
    }

    @Test
    void testHandleMissingParam() {
        org.springframework.web.bind.MissingServletRequestParameterException exception =
            new org.springframework.web.bind.MissingServletRequestParameterException("param", "String");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleMissingParam(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required parameter: param", response.getBody().message());
    }

    @Test
    void testHandleConstraintViolation() {
        jakarta.validation.ConstraintViolationException exception =
            new jakarta.validation.ConstraintViolationException("Validation failed", java.util.Set.of());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleConstraintViolation(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().message());
    }

    @Test
    void testHandleMissingHeader() {
        org.springframework.web.bind.MissingRequestHeaderException exception =
            new org.springframework.web.bind.MissingRequestHeaderException("Authorization", null);

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleMissingHeader(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing required header: Authorization", response.getBody().message());
    }

    @Test
    void testHandleImageNotFound() {
        GlobalExceptionHandler.ImageNotFoundException exception =
            new GlobalExceptionHandler.ImageNotFoundException("Image not found");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleImageNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Image not found", response.getBody().message());
    }

    @Test
    void testHandleImageLimitExceeded() {
        GlobalExceptionHandler.ImageLimitExceededException exception =
            new GlobalExceptionHandler.ImageLimitExceededException("Limit exceeded");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleImageLimitExceeded(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Limit exceeded", response.getBody().message());
    }

    @Test
    void testHandleImageUploadFailure() {
        GlobalExceptionHandler.ImageUploadException exception =
            new GlobalExceptionHandler.ImageUploadException("Upload failed", new RuntimeException());

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
            handler.handleImageUploadFailure(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Image upload failed", response.getBody().message());
    }

    @Test
    void handleIllegalArgument_returnsBadRequestWithMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException ex = new IllegalArgumentException("File is empty");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File is empty", response.getBody().message());
    }

    @Test
    void handleIllegalArgument_preservesExactMessage_forFileSizeError() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        IllegalArgumentException ex = new IllegalArgumentException("File size exceeds maximum limit of 10MB");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgument(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("File size exceeds maximum limit of 10MB", response.getBody().message());
    }
}
