package com.tarantulacommunity.tarantulasocial.features.message.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

class CreateThreadRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void noViolations_whenAllFieldsValid() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello there", "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void noViolations_whenSubjectIsNull() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello there", null);

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void violation_whenRecipientIdIsNull() {
        CreateThreadRequest request = new CreateThreadRequest(null, "Hello there", "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("recipientId");
    }

    @Test
    void violation_whenContentIsBlank() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "   ", "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void violation_whenContentIsNull() {
        CreateThreadRequest request = new CreateThreadRequest(2L, null, "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void violation_whenContentExceeds2000Characters() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "a".repeat(2001), "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("content");
    }

    @Test
    void noViolation_whenContentIsExactly2000Characters() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "a".repeat(2000), "Subject");

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void violation_whenSubjectExceeds255Characters() {
        CreateThreadRequest request = new CreateThreadRequest(2L, "Hello", "a".repeat(256));

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("subject");
    }

    @Test
    void multipleViolations_whenRecipientIdAndContentBothInvalid() {
        CreateThreadRequest request = new CreateThreadRequest(null, "", null);

        Set<ConstraintViolation<CreateThreadRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }
}
