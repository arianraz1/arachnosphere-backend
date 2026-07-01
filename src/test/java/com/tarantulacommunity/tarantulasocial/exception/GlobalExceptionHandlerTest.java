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
    void testHandleNotFound() {
        NoHandlerFoundException exception = new NoHandlerFoundException("GET", "/api/test", null);
        
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = 
            handler.handleNotFound(exception);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("The requested endpoint was not found", response.getBody().message());
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
}
