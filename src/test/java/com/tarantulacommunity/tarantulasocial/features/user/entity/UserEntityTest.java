package com.tarantulacommunity.tarantulasocial.features.user.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserEntityTest {

    @Autowired
    private TestEntityManager em;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void verifiedUserPersistsAndMatchesSuccessfully() {
        UserEntity user = TestUtilities.verifiedUser("1", "1");
        UserEntity savedUser = em.persistFlushFind(user);

        assertNotNull(savedUser.getId());
        assertNotNull(savedUser.getCreatedAt());
        assertNotNull(savedUser.getUpdatedAt());
        assertEquals(user.getUsername(), savedUser.getUsername());
        assertEquals(user.getEmail(), savedUser.getEmail());
        assertEquals(0L, savedUser.getVersion());
    }

    @Test
    void duplicateEmailViolatesUniqueConstraint() {
        UserEntity user1 = TestUtilities.verifiedUser("2", "2");
        UserEntity user2 = TestUtilities.verifiedUser("2", "3");

        em.persist(user1);
        assertThrows(Exception.class, () -> em.persistAndFlush(user2));
    }

    @Test
    void duplicateUsernameViolatesUniqueConstraint() {
        UserEntity user1 = TestUtilities.verifiedUser("4", "4");
        UserEntity user2 = TestUtilities.verifiedUser("5", "4");

        em.persist(user1);
        assertThrows(Exception.class, () -> em.persistAndFlush(user2));
    }

    @Test
    void invalidEmailFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("6", "6");
        user.setEmail("invalid");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void emailTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("7", "7");
        user.setEmail("a@test" + ".com".repeat(25)); // exceeds max length of 100, but passes @Email annotation

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameTooShortFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("8", "8");
        user.setUsername("ab");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("9", "9");
        user.setUsername("a".repeat(51)); // exceeds max length of 50

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void usernameWithInvalidCharsFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("10", "10");
        user.setUsername("This is invalid");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void blankPasswordFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("11", "11");
        user.setPassword("");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void blankEmailFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("12", "12");
        user.setEmail("");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
    }

    @Test
    void blankUsernameFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("13", "13");
        user.setUsername("");

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertFalse(violations.isEmpty()); // could trigger @NotBlank, @Pattern, and @Size(min=3) simultaneously
    }

    @Test
    void bioTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("14", "14");
        user.setBio("a".repeat(1001)); // exceeds max length of 1000

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void profilePicUrlTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("15", "15");
        user.setProfilePicUrl("a".repeat(2049)); // exceeds max length of 2048

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void favoriteImageUrlTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("16", "16");
        user.setFavoriteImageUrl("a".repeat(2049)); // exceeds max length of 2048

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void favoriteCaptionTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("17", "17");
        user.setFavoriteCaption("a".repeat(256)); // exceeds max length of 255

        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);
        assertEquals(1, violations.size());
    }

    @Test
    void verifiedUserWithOptionalFieldsPersistsSuccessfully() {
        UserEntity user = TestUtilities.verifiedUser("18", "18");
        user.setBio("I love my Brazilian Black T!");
        user.setProfilePicUrl("https://example.com/pic.jpg");
        user.setFavoriteImageUrl("https://example.com/favorite.jpg");
        user.setFavoriteCaption("My GBB, Juniper");

        UserEntity savedUser = em.persistFlushFind(user);

        assertEquals(user.getBio(), savedUser.getBio());
        assertEquals(user.getProfilePicUrl(), savedUser.getProfilePicUrl());
        assertEquals(user.getFavoriteImageUrl(), savedUser.getFavoriteImageUrl());
        assertEquals(user.getFavoriteCaption(), savedUser.getFavoriteCaption());
        assertEquals(0L, savedUser.getVersion());
    }

    @Test
    void emailVerifiedDefaultsToFalse() {
        UserEntity user = TestUtilities.validUser("supertest", "19", false);
        UserEntity savedUser = em.persistFlushFind(user);

        assertFalse(savedUser.isEmailVerified());
    }
}

