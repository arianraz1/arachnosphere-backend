package com.tarantulacommunity.tarantulasocial.feature.collection_entry.dto;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateCollectionEntryRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validCreateCollectionEntryRequestPasses() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setAdultFemale(1);
        request.setFeaturedImageUrl("https://example.com/spider.jpg");
        request.setCustomInfo("Kept at 78F");

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullSpeciesIdFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(null);
        request.setAdultFemale(1);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeSlingMaleFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(-1);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeJuvenileCountFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setJuvenileFemale(-5);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeSubadultCountFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSubadultUnsexed(-2);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeAdultCountFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setAdultMale(-10);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void multipleNegativeCountsFailValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(-1);
        request.setJuvenileFemale(-2);
        request.setAdultMale(-3);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
    }

    @Test
    void zeroCountsPassValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(0);
        request.setJuvenileFemale(0);
        request.setAdultMale(0);

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void featuredImageUrlTooLongFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setFeaturedImageUrl("a".repeat(2049)); // exceeds 2048

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void customInfoTooLongFailsValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setCustomInfo("a".repeat(1001)); // exceeds 1000

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void allFieldsPopulatedPassesValidation() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(2);
        request.setSlingFemale(1);
        request.setSlingUnsexed(3);
        request.setJuvenileMale(1);
        request.setJuvenileFemale(2);
        request.setJuvenileUnsexed(0);
        request.setSubadultMale(0);
        request.setSubadultFemale(1);
        request.setSubadultUnsexed(0);
        request.setAdultMale(0);
        request.setAdultFemale(1);
        request.setAdultUnsexed(0);
        request.setFeaturedImageUrl("https://example.com/spider.jpg");
        request.setCustomInfo("Kept at 78F, fed weekly");

        Set<ConstraintViolation<CreateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}

