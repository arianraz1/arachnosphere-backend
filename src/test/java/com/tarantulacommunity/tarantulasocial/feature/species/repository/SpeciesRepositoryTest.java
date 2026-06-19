package com.tarantulacommunity.tarantulasocial.feature.species.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));

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

        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));

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

        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<SpeciesEntity> result = speciesRepository.findOwnedSpeciesByQuery("Green Bottle Blue", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(gbb, result.getContent().getFirst());
    }

    @Test
    void findOwnedSpeciesByQuery_isCaseInsensitive() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));
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

        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));
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
        UserEntity user = em.persistFlushFind(TestUtilities.validUser("1", "1"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<SpeciesEntity> result = speciesRepository.findUnownedSpeciesByQuery("", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}
