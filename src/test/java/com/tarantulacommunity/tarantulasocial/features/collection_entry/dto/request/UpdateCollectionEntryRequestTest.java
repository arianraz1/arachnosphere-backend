package com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateCollectionEntryRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validUpdateCollectionEntryRequestPasses() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setAdultFemale(2);
        request.setCustomInfo("Updated info");

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void emptyUpdateRequestPasses() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeSlingMaleFailsValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(-1);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeJuvenileCountFailsValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setJuvenileFemale(-3);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeSubadultCountFailsValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSubadultUnsexed(-1);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void negativeAdultCountFailsValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setAdultMale(-5);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void multipleNegativeCountsFailValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(-1);
        request.setAdultFemale(-2);
        request.setJuvenileUnsexed(-3);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
    }

    @Test
    void zeroCountsPassValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(0);
        request.setAdultFemale(0);
        request.setJuvenileUnsexed(0);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void positiveCountsPassValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(5);
        request.setJuvenileFemale(3);
        request.setAdultMale(1);

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void customInfoTooLongFailsValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setCustomInfo("a".repeat(1001)); // exceeds 1000

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void customInfoAtMaxLengthPassesValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setCustomInfo("a".repeat(1000)); // exactly at max

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void allFieldsPopulatedPassesValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(1);
        request.setSlingFemale(2);
        request.setSlingUnsexed(0);
        request.setJuvenileMale(0);
        request.setJuvenileFemale(1);
        request.setJuvenileUnsexed(2);
        request.setSubadultMale(0);
        request.setSubadultFemale(0);
        request.setSubadultUnsexed(1);
        request.setAdultMale(1);
        request.setAdultFemale(1);
        request.setAdultUnsexed(0);
        request.setCustomInfo("Updated collection info");

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void negativeCountsAndLongCustomInfoFailValidation() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(-1);
        request.setCustomInfo("b".repeat(1001));

        Set<ConstraintViolation<UpdateCollectionEntryRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}

