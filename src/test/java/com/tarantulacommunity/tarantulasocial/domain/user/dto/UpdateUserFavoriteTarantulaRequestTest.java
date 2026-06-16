package com.tarantulacommunity.tarantulasocial.domain.user.dto;

import com.tarantulacommunity.tarantulasocial.domain.user.dto.request.UpdateUserFavoriteTarantulaRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserFavoriteTarantulaRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validFavoriteTarantulaRequestPasses() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("https://example.com/favorite.jpg");
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
    void favoriteImageUrlTooLongFailsValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("a".repeat(2049)); // exceeds 2048
        request.setFavoriteCaption("Caption");

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void favoriteImageUrlAt2048CharsPassesValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("a".repeat(2048)); // exactly 2048 is valid
        request.setFavoriteCaption("Caption");

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void favoriteCaptionTooLongFailsValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("https://example.com/favorite.jpg");
        request.setFavoriteCaption("a".repeat(256)); // exceeds 255

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void favoriteCaptionAt255CharsPassesValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("https://example.com/favorite.jpg");
        request.setFavoriteCaption("a".repeat(255)); // exactly 255 is valid

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void bothFieldsTooLongFailsValidation() {
        UpdateUserFavoriteTarantulaRequest request = new UpdateUserFavoriteTarantulaRequest();
        request.setFavoriteImageUrl("a".repeat(2049));
        request.setFavoriteCaption("b".repeat(256));

        Set<ConstraintViolation<UpdateUserFavoriteTarantulaRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}

