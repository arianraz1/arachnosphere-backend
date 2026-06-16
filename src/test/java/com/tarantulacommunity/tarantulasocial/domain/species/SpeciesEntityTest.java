package com.tarantulacommunity.tarantulasocial.domain.species;

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
public class SpeciesEntityTest {

    @Autowired
    private TestEntityManager em;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validSpeciesPersistsAndMatchesSuccessfully() {
        SpeciesEntity species = TestUtilities.validSpecies(1001L, "Grammostola pulchripes");
        SpeciesEntity saved = em.persistFlushFind(species);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());
        assertEquals(species.getScientificName(), saved.getScientificName());
        assertEquals(species.getGbifNubKey(), saved.getGbifNubKey());
        assertEquals(0L, saved.getVersion());
    }

    @Test
    void duplicateScientificNameViolatesUniqueConstraint() {
        SpeciesEntity species1 = TestUtilities.validSpecies(1002L, "Brachypelma hamorii");
        SpeciesEntity species2 = TestUtilities.validSpecies(1003L, "Brachypelma hamorii"); // dup name, diff gbif key

        em.persist(species1);
        assertThrows(Exception.class, () -> em.persistAndFlush(species2));
    }

    @Test
    void duplicateGbifNubKeyViolatesUniqueConstraint() {
        SpeciesEntity species1 = TestUtilities.validSpecies(1004L, "Aphonopelma chalcodes");
        SpeciesEntity species2 = TestUtilities.validSpecies(1004L, "Aphonopelma seemanni"); // dup gbif key, diff name

        em.persist(species1);
        assertThrows(Exception.class, () -> em.persistAndFlush(species2));
    }

    @Test
    void blankScientificNameFailsBeanValidation() {
        SpeciesEntity species = TestUtilities.validSpecies(1005L, "");

        Set<ConstraintViolation<SpeciesEntity>> violations = validator.validate(species);
        assertEquals(1, violations.size());
    }

    @Test
    void scientificNameTooLongFailsBeanValidation() {
        SpeciesEntity species = TestUtilities.validSpecies(1006L, "a".repeat(256)); // exceeds max length of 255

        Set<ConstraintViolation<SpeciesEntity>> violations = validator.validate(species);
        assertEquals(1, violations.size());
    }

    @Test
    void commonNameTooLongFailsBeanValidation() {
        SpeciesEntity species = TestUtilities.validSpecies(1007L, "Caribena versicolor");
        species.setCommonName("a".repeat(256)); // exceeds max length of 255

        Set<ConstraintViolation<SpeciesEntity>> violations = validator.validate(species);
        assertEquals(1, violations.size());
    }

    @Test
    void imageUrlTooLongFailsBeanValidation() {
        SpeciesEntity species = TestUtilities.validSpecies(1008L, "Poecilotheria metallica");
        species.setImageUrl("a".repeat(2049)); // exceeds max length of 2048

        Set<ConstraintViolation<SpeciesEntity>> violations = validator.validate(species);
        assertEquals(1, violations.size());
    }

    @Test
    void validSpeciesWithOptionalFieldsPersistsSuccessfully() {
        SpeciesEntity species = TestUtilities.validSpecies(1009L, "Tliltocatl albopilosus");
        species.setCommonName("Curly Hair Tarantula");
        species.setImageUrl("https://example.com/curly-hair.jpg");
        species.setDescription("A hardy, beginner-friendly New World species known for its docile temperament.");

        SpeciesEntity saved = em.persistFlushFind(species);

        assertEquals(species.getCommonName(), saved.getCommonName());
        assertEquals(species.getImageUrl(), saved.getImageUrl());
        assertEquals(species.getDescription(), saved.getDescription());
    }

    @Test
    void nullGbifNubKeyFailsBeanValidation() {
        SpeciesEntity species = TestUtilities.validSpecies(1010L, "Pamphobeteus antinous");
        species.setGbifNubKey(null);

        Set<ConstraintViolation<SpeciesEntity>> violations = validator.validate(species);
        assertEquals(1, violations.size());
    }
}
