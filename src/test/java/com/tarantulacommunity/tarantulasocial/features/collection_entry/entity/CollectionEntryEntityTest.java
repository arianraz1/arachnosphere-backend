package com.tarantulacommunity.tarantulasocial.features.collection_entry.entity;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
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
class CollectionEntryEntityTest {

    @Autowired
    private TestEntityManager em;

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void validEntryPersistsWithDefaults() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2001L, "Grammostola pulchripes"));

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        CollectionEntryEntity saved = em.persistFlushFind(entry);

        assertNotNull(saved.getId());
        assertEquals(0, saved.getSlingMale());
        assertEquals(0, saved.getSlingFemale());
        assertEquals(0, saved.getSlingUnsexed());
        assertEquals(0, saved.getJuvenileMale());
        assertEquals(0, saved.getJuvenileFemale());
        assertEquals(0, saved.getJuvenileUnsexed());
        assertEquals(0, saved.getSubadultMale());
        assertEquals(0, saved.getSubadultFemale());
        assertEquals(0, saved.getSubadultUnsexed());
        assertEquals(0, saved.getAdultMale());
        assertEquals(0, saved.getAdultFemale());
        assertEquals(0, saved.getAdultUnsexed());
        assertNotNull(saved.getImages());
        assertTrue(saved.getImages().isEmpty());
    }

    @Test
    void duplicateUserSpeciesViolatesUniqueConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2002L, "Brachypelma hamorii"));

        em.persist(TestUtilities.validCollectionEntry(user, species));

        CollectionEntryEntity dup = TestUtilities.validCollectionEntry(user, species);
        assertThrows(Exception.class, () -> em.persistAndFlush(dup));
    }

    @Test
    void sameSpeciesDifferentUsersPersistsSuccessfully() {
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("3", "3"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("4", "4"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2003L, "Aphonopelma chalcodes"));

        CollectionEntryEntity entry1 = TestUtilities.validCollectionEntry(user1, species);
        CollectionEntryEntity entry2 = TestUtilities.validCollectionEntry(user2, species);

        em.persistAndFlush(entry1);
        assertDoesNotThrow(() -> em.persistAndFlush(entry2));
    }

    @Test
    void sameUserDifferentSpeciesPersistsSuccessfully() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("5", "5"));
        SpeciesEntity species1 = em.persistFlushFind(TestUtilities.validSpecies(2004L, "Caribena versicolor"));
        SpeciesEntity species2 = em.persistFlushFind(TestUtilities.validSpecies(2005L, "Poecilotheria metallica"));

        CollectionEntryEntity entry1 = TestUtilities.validCollectionEntry(user, species1);
        CollectionEntryEntity entry2 = TestUtilities.validCollectionEntry(user, species2);

        em.persistAndFlush(entry1);
        assertDoesNotThrow(() -> em.persistAndFlush(entry2));
    }

    @Test
    void negativeCountViolatesCheckConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("6", "6"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2006L, "Tliltocatl albopilosus"));

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setAdultMale(-1);

        assertThrows(Exception.class, () -> em.persistAndFlush(entry));
    }

    @Test
    void negativeCountFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("7", "7");
        SpeciesEntity species = TestUtilities.validSpecies(2007L, "Xenesthis immanis");

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setJuvenileFemale(-5);

        Set<ConstraintViolation<CollectionEntryEntity>> violations = validator.validate(entry);
        assertEquals(1, violations.size());
    }

    @Test
    void customInfoTooLongFailsBeanValidation() {
        UserEntity user = TestUtilities.verifiedUser("8", "8");
        SpeciesEntity species = TestUtilities.validSpecies(2008L, "Pamphobeteus antinous");

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setCustomInfo("a".repeat(1001)); // exceeds max length of 1000

        Set<ConstraintViolation<CollectionEntryEntity>> violations = validator.validate(entry);
        assertEquals(1, violations.size());
    }

    @Test
    void validEntryWithCountsAndOptionalFieldsPersistsSuccessfully() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("10", "10"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2010L, "Theraphosa blondi"));

        CollectionEntryEntity entry = TestUtilities.validCollectionEntry(user, species);
        entry.setAdultFemale(1);
        entry.setSlingUnsexed(3);
        entry.setCustomInfo("Kept at 78F, fed weekly");

        CollectionEntryEntity saved = em.persistFlushFind(entry);

        assertEquals(1, saved.getAdultFemale());
        assertEquals(3, saved.getSlingUnsexed());
        assertEquals("Kept at 78F, fed weekly", saved.getCustomInfo());
    }

    @Test
    void nullUserViolatesNotNullConstraint() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2011L, "Cyriocosmus elegans"));

        CollectionEntryEntity entry = new CollectionEntryEntity();
        entry.setSpecies(species);
        // user left null

        assertThrows(Exception.class, () -> em.persistAndFlush(entry));
    }

    @Test
    void nullSpeciesViolatesNotNullConstraint() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("11", "11"));

        CollectionEntryEntity entry = new CollectionEntryEntity();
        entry.setUser(user);
        // species left null

        assertThrows(Exception.class, () -> em.persistAndFlush(entry));
    }

    @Test
    void version_incrementsOnUpdate() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Long versionBefore = entry.getVersion();

        entry.setAdultMale(3);
        CollectionEntryEntity updated = em.persistFlushFind(entry);

        assertNotNull(updated.getVersion());
        assertEquals(versionBefore + 1, updated.getVersion());
    }

    // ---------- images relationship (replaces the old featuredImageUrl field) ----------

    @Test
    void images_cascadePersistsWhenAddedToEntry() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("12", "12"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2012L, "Nhandu chromatus"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        CollectionImageEntity image = CollectionImageEntity.builder()
                .collection(entry)
                .s3Key("collection-images/x/photo.jpg")
                .displayOrder(0)
                .build();
        entry.getImages().add(image);

        CollectionEntryEntity saved = em.persistFlushFind(entry);

        assertEquals(1, saved.getImages().size());
        assertEquals("collection-images/x/photo.jpg", saved.getImages().getFirst().getS3Key());
    }

    @Test
    void images_orphanRemoval_deletesImageWhenRemovedFromList() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("13", "13"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(2013L, "Euathlus sp."));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        CollectionImageEntity image = CollectionImageEntity.builder()
                .collection(entry)
                .s3Key("collection-images/y/photo.jpg")
                .displayOrder(0)
                .build();
        entry.getImages().add(image);
        entry = em.persistFlushFind(entry);

        Long imageId = entry.getImages().getFirst().getId();
        entry.getImages().clear();
        em.persistAndFlush(entry);

        assertNull(em.find(CollectionImageEntity.class, imageId));
    }
}