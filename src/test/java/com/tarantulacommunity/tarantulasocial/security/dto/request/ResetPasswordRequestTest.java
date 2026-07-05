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
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "password123");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankTokenFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("", "password123");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void nullTokenFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest(null, "password123");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordTooShortFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "short");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordTooLongFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "a".repeat(73));

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordAt8CharsPassesValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "a".repeat(8));

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void passwordAt72CharsPassesValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "a".repeat(72));

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankPasswordFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "");

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void nullPasswordFailsValidation() {
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", null);

        Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }
}
