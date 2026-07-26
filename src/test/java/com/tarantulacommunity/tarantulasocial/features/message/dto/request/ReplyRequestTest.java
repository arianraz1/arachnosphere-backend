package com.tarantulacommunity.tarantulasocial.features.message.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ReplyRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void noViolations_whenContentValid() {
        ReplyRequest request = new ReplyRequest("Hello there");

        Set<ConstraintViolation<ReplyRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void violation_whenContentIsBlank() {
        ReplyRequest request = new ReplyRequest("   ");

        Set<ConstraintViolation<ReplyRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void violation_whenContentIsNull() {
        ReplyRequest request = new ReplyRequest(null);

        Set<ConstraintViolation<ReplyRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void violation_whenContentExceeds2000Characters() {
        ReplyRequest request = new ReplyRequest("a".repeat(2001));

        Set<ConstraintViolation<ReplyRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void noViolation_whenContentIsExactly2000Characters() {
        ReplyRequest request = new ReplyRequest("a".repeat(2000));

        Set<ConstraintViolation<ReplyRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }
}
