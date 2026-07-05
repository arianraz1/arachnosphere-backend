package com.tarantulacommunity.tarantulasocial.security.dto.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldPassValidRequest() {
        LoginRequest req = new LoginRequest("user@example.com", "pass123");
        assertThat(validator.validate(req)).isEmpty();
    }

    @Test
    void shouldFailInvalidEmail() {
        LoginRequest req = new LoginRequest("not-an-email", "pass");
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void shouldFailBlankPassword() {
        LoginRequest req = new LoginRequest("user@example.com", "");
        assertThat(validator.validate(req)).isNotEmpty();
    }
}