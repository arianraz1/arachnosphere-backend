package com.tarantulacommunity.tarantulasocial.feature.message.dto;

import com.tarantulacommunity.tarantulasocial.features.message.dto.request.SendMessageRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendMessageRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validSendMessageRequestPasses() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(123L);
        request.setContent("Hey, nice collection!");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullRecipientIdFailsValidation() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(null);
        request.setContent("Hey!");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void blankContentFailsValidation() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(123L);
        request.setContent("");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void contentTooLongFailsValidation() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(123L);
        request.setContent("a".repeat(2001)); // exceeds 2000

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void contentAt2000CharsPassesValidation() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(123L);
        request.setContent("a".repeat(2000)); // exactly 2000 is valid

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void zeroRecipientIdPassesValidation() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(0L); // @NotNull just checks for null, not if ID is valid
        request.setContent("Hello");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void nullRecipientIdAndBlankContentBothFail() {
        SendMessageRequest request = new SendMessageRequest();
        request.setRecipientId(null);
        request.setContent("");

        Set<ConstraintViolation<SendMessageRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
    }
}

