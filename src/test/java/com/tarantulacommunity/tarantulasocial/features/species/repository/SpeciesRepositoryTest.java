package com.tarantulacommunity.tarantulasocial.features.species.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class SpeciesRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SpeciesRepository speciesRepository;

    @Test
    void findByGbifNubKey_returnsCorrectSpecies() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Optional<SpeciesEntity> result = speciesRepository.findByGbifNubKey(1L);

        assertTrue(result.isPresent());
        assertEquals(species, result.get());
    }

    @Test
    void findByGbifNubKey_returnsEmpty_whenNoMatch() {
        Optional<SpeciesEntity> result = speciesRepository.findByGbifNubKey(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByScientificName_returnsCorrectSpecies() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Optional<SpeciesEntity> result = speciesRepository.findByScientificName("Chromatopelma cyaneopubescens");

        assertTrue(result.isPresent());
        assertEquals(species, result.get());
    }

    @Test
    void findByScientificName_returnsEmpty_whenNoMatch() {
        Optional<SpeciesEntity> result = speciesRepository.findByScientificName("nonexistent");

        assertTrue(result.isEmpty());
    }

    // --- query tests ---

    @Test
    void findOwnedSpeciesByQuery_returnsOnlyOwnedSpecies() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity owned = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity unowned = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, owned));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(owned, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQuery_filtersCorrectlyByScientificName() {
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("Chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQuery_filtersCorrectlyByCommonName() {
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        gbb.setCommonName("Green Bottle Blue");
        gbb = em.persistFlushFind(gbb);

        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        bh.setCommonName("Mexican Red Knee");
        em.persistFlushFind(bh);

        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("Green Bottle Blue", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQuery_isCaseInsensitive() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findOwnedSpeciesByQuery_returnsEmpty_whenNoOwned() {
        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findUnownedSpeciesByQuery_filtersCorrectlyByScientificName() {
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("Brachypelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(bh, result.getContent().getFirst());
    }

    @Test
    void findUnownedSpeciesByQuery_returnsOnlyUnownedSpecies() {
        SpeciesEntity owned = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity unowned = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, owned));

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(unowned, result.getContent().getFirst());
    }

    @Test
    void findUnownedSpeciesByQuery_filtersCorrectlyByCommonName() {
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        gbb.setCommonName("Green Bottle Blue");
        gbb = em.persistFlushFind(gbb);

        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        bh.setCommonName("Mexican Red Knee");
        bh = em.persistFlushFind(bh);

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("Mexican Red Knee", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(bh, result.getContent().getFirst());
    }

    @Test
    void findUnownedSpeciesByQuery_isCaseInsensitive() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findUnownedSpeciesByQuery_returnsEmpty_whenAllSpeciesAreOwned() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findOwnedSpeciesByQueryOrderByOwnerCount_returnsOnlyOwnedSpecies() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity owned = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, owned));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(owned, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQueryOrderByOwnerCount_ordersbyOwnerCountDescending() {
        SpeciesEntity popular = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity lessPopular = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "user1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "user2"));
        UserEntity user3 = em.persistFlushFind(TestUtilities.verifiedUser("3", "user3"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user1, popular));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user2, popular));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user3, popular));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user1, lessPopular));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(popular, result.getContent().get(0));
        assertEquals(lessPopular, result.getContent().get(1));
    }

    @Test
    void findOwnedSpeciesByQueryOrderByOwnerCount_filtersbyScientificName() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("Chromato", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQueryOrderByOwnerCount_isCaseInsensitive() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findOwnedSpeciesByQueryOrderByOwnerCount_returnsEmpty_whenNoOwned() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByGbifNubKey_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        assertTrue(speciesRepository.existsByGbifNubKey(1L));
    }

    @Test
    void existsByGbifNubKey_returnsFalse_whenNotExists() {
        assertFalse(speciesRepository.existsByGbifNubKey(99L));
    }

// --- existsByScientificName ---

    @Test
    void existsByScientificName_returnsTrue_whenExists() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        assertTrue(speciesRepository.existsByScientificName("Chromatopelma cyaneopubescens"));
    }

    @Test
    void existsByScientificName_returnsFalse_whenNotExists() {
        assertFalse(speciesRepository.existsByScientificName("nonexistent"));
    }

    // --- findAllByImageUrlIsNull ---

    @Test
    void findAllByImageUrlIsNull_returnsOnlySpeciesWithoutImage() {
        SpeciesEntity withoutImage = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        SpeciesEntity withImage = TestUtilities.validSpecies(2L, "Brachypelma hamorii");
        withImage.setImageUrl("https://cdn/img.jpg");
        em.persistFlushFind(withImage);

        List<SpeciesEntity> result = speciesRepository.findAllByImageUrlIsNull();

        assertEquals(1, result.size());
        assertEquals(withoutImage, result.getFirst());
    }

    @Test
    void findAllByImageUrlIsNull_returnsEmpty_whenAllSpeciesHaveImages() {
        SpeciesEntity withImage = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        withImage.setImageUrl("https://cdn/img.jpg");
        em.persistFlushFind(withImage);

        List<SpeciesEntity> result = speciesRepository.findAllByImageUrlIsNull();

        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByImageUrlIsNull_returnsEmpty_whenNoSpeciesExist() {
        List<SpeciesEntity> result = speciesRepository.findAllByImageUrlIsNull();

        assertTrue(result.isEmpty());
    }

// --- findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase ---

    @Test
    void findByScientificNameOrCommonNameContaining_matchesByScientificName() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository
                .findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
                        "Chromatopelma", "Chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(species, result.getContent().getFirst());
    }

    @Test
    void findByScientificNameOrCommonNameContaining_matchesByCommonName() {
        SpeciesEntity species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        species.setCommonName("Green Bottle Blue");
        species = em.persistFlushFind(species);

        Page<SpeciesEntity> result = speciesRepository
                .findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
                        "Green Bottle Blue", "Green Bottle Blue", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(species, result.getContent().getFirst());
    }

    @Test
    void findByScientificNameOrCommonNameContaining_isCaseInsensitive() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository
                .findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
                        "chromatopelma", "chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByScientificNameOrCommonNameContaining_returnsEmpty_whenNoMatch() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository
                .findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
                        "nonexistent", "nonexistent", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

// --- searchAllSpecies (the @Query the service actually delegates to) ---

    @Test
    void searchAllSpecies_returnsSpeciesRegardlessOfOwnership() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity owned = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity unowned = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, owned));

        Page<SpeciesEntity> result = speciesRepository.searchAllSpecies("", PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().containsAll(List.of(owned, unowned)));
    }

    @Test
    void searchAllSpecies_filtersByScientificName() {
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        Page<SpeciesEntity> result = speciesRepository.searchAllSpecies("Chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void searchAllSpecies_filtersByCommonName() {
        SpeciesEntity gbb = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        gbb.setCommonName("Green Bottle Blue");
        gbb = em.persistFlushFind(gbb);
        em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        Page<SpeciesEntity> result = speciesRepository.searchAllSpecies("Green Bottle Blue", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void searchAllSpecies_isCaseInsensitive() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository.searchAllSpecies("chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void searchAllSpecies_returnsEmpty_whenNoMatch() {
        em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Page<SpeciesEntity> result = speciesRepository.searchAllSpecies("nonexistent", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}
