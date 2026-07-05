package com.tarantulacommunity.tarantulasocial.security.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ResetPasswordRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRequestPassesValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "password123");

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankTokenFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("", "password123");

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void nullTokenFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest(null, "password123");

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordTooShortFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "short");

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordTooLongFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "a".repeat(73));

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordAt8CharsPassesValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "a".repeat(8));

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordAt72CharsPassesValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "a".repeat(72));

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankPasswordFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", "");

        Set<ConstraintViolation<com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullPasswordFailsValidation() {
        com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest request = new com.tarantulacommunity.tarantulasocial.security.dto.request.ResetPasswordRequest("valid-token", null);

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }
}
