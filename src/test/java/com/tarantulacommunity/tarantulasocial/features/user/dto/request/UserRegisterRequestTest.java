package com.tarantulacommunity.tarantulasocial.features.user.dto.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserRegisterRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validRegisterRequestPasses() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankEmailFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("");
        request.setUsername("testuser");
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void invalidEmailFormatFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("notanemail");
        request.setUsername("testuser");
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void emailTooLongFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("a@test" + ".com".repeat(25)); // exceeds max length of 100, but passes @Email annotation
        request.setUsername("testuser");
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void blankUsernameFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("");
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void usernameTooShortFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("ab"); // less than 3
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameTooLongFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("a".repeat(51)); // exceeds 50
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameWithInvalidCharsFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("user name!"); // contains space and !
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameWithValidSpecialCharsPassesValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("user_name-test.123"); // underscores, hyphens, dots, numbers are valid
        request.setPassword("ValidPass123");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void blankPasswordFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("");

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passwordTooShortFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("Pass123"); // only 7 chars, needs min 8

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordTooLongFailsValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("a".repeat(73)); // exceeds 72 (BCrypt limit)

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
    }

    @Test
    void passwordAt72CharsPassesValidation() {
        UserRegisterRequest request = new UserRegisterRequest();
        request.setEmail("test@example.com");
        request.setUsername("testuser");
        request.setPassword("a".repeat(72)); // exactly 72 is valid

        Set<ConstraintViolation<UserRegisterRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }
}

