package com.tarantulacommunity.tarantulasocial.features.user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateUserFavoriteTarantulaRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validFavoriteTarantulaRequestPasses() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption("My beautiful GBB");

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyRequestPasses() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void favoriteCaptionTooLongFailsValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption("a".repeat(256)); // exceeds 255

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void favoriteCaptionAt255CharsPassesValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteCaption("a".repeat(255)); // exactly 255 is valid

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}

