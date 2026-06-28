package com.tarantulacommunity.tarantulasocial.features.user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateUserProfileRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validUpdateRequestPasses() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("https://example.com/pic.jpg");
        request.setBio("I love tarantulas!");

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyRequestPasses() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void profilePicUrlTooLongFailsValidation() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("a".repeat(2049)); // exceeds 2048
        request.setBio("Bio");

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void profilePicUrlAt2048CharsPassesValidation() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("a".repeat(2048)); // exactly 2048 is valid
        request.setBio("Bio");

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void bioTooLongFailsValidation() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("https://example.com/pic.jpg");
        request.setBio("a".repeat(1001)); // exceeds 1000

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void bioAt1000CharsPassesValidation() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("https://example.com/pic.jpg");
        request.setBio("a".repeat(1000)); // exactly 1000 is valid

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void bothFieldsTooLongFailsValidation() {
        UpdateUserProfileRequest request = new UpdateUserProfileRequest();
        request.setProfilePicUrl("a".repeat(2049));
        request.setBio("b".repeat(1001));

        Set<ConstraintViolation<UpdateUserProfileRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}

