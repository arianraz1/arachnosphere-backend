package com.tarantulacommunity.tarantulasocial.features.collection_entry.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
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
public class CollectionEntryRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CollectionEntryRepository repository;

    @Test
    void findByUser_returnsAllEntriesForUser() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        CollectionEntryEntity entry1 = em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        CollectionEntryEntity entry2 = em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<CollectionEntryEntity> result = repository.findByUser(user, PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().containsAll(List.of(entry1, entry2)));
    }

    @Test
    void findByUser_returnsEmpty_whenUserHasNoEntries() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        Page<CollectionEntryEntity> result = repository.findByUser(user, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUser_doesNotReturnOtherUsersEntries() {
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user2, gbb));

        Page<CollectionEntryEntity> result = repository.findByUser(user1, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUser_respectsPageSize() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        SpeciesEntity ph = em.persistFlushFind(TestUtilities.validSpecies(3L, "Poecilotheria metallica"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, ph));

        Page<CollectionEntryEntity> result = repository.findByUser(user, PageRequest.of(0, 2));

        assertEquals(3, result.getTotalElements());
        assertEquals(2, result.getContent().size());
    }

    @Test
    void findByUser_supportsPagination() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));
        SpeciesEntity ph = em.persistFlushFind(TestUtilities.validSpecies(3L, "Poecilotheria metallica"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, ph));

        Page<CollectionEntryEntity> page1 = repository.findByUser(user, PageRequest.of(0, 2));
        Page<CollectionEntryEntity> page2 = repository.findByUser(user, PageRequest.of(1, 2));

        assertEquals(3, page1.getTotalElements());
        assertEquals(2, page1.getContent().size());
        assertEquals(1, page2.getContent().size());
    }

    @Test
    void findByUserAndSpecies_returnsCorrectEntry() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Optional<CollectionEntryEntity> result = repository.findByUserAndSpecies(user, species);

        assertTrue(result.isPresent());
        assertEquals(entry, result.get());
    }

    @Test
    void findByUserAndSpecies_returnsEmpty_whenNoMatch() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        Optional<CollectionEntryEntity> result = repository.findByUserAndSpecies(user, species);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserAndSpecies_doesNotReturnOtherUsersEntries() {
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user2, species));

        Optional<CollectionEntryEntity> result = repository.findByUserAndSpecies(user1, species);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByUserAndSpecies_returnsTrue_whenExists() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        assertTrue(repository.existsByUserAndSpecies(user, species));
    }

    @Test
    void existsByUserAndSpecies_returnsFalse_whenNotExists() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        assertFalse(repository.existsByUserAndSpecies(user, species));
    }

    // ---------- existsByIdAndUserId (missing from original suite; backs the
    // ownership check used everywhere in CollectionImageService) ----------

    @Test
    void existsByIdAndUserId_returnsTrue_whenEntryBelongsToUser() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        assertTrue(repository.existsByIdAndUserId(entry.getId(), user.getId()));
    }

    @Test
    void existsByIdAndUserId_returnsFalse_whenEntryBelongsToDifferentUser() {
        UserEntity owner = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity otherUser = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(owner, species));

        assertFalse(repository.existsByIdAndUserId(entry.getId(), otherUser.getId()));
    }

    @Test
    void existsByIdAndUserId_returnsFalse_whenEntryDoesNotExist() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));

        assertFalse(repository.existsByIdAndUserId(999L, user.getId()));
    }

    @Test
    void findBySpecies_returnsAllEntriesForSpecies() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));

        CollectionEntryEntity entry1 = em.persistFlushFind(TestUtilities.validCollectionEntry(user1, species));
        CollectionEntryEntity entry2 = em.persistFlushFind(TestUtilities.validCollectionEntry(user2, species));

        List<CollectionEntryEntity> result = repository.findBySpecies(species);

        assertEquals(2, result.size());
        assertTrue(result.containsAll(List.of(entry1, entry2)));
    }


    @Test
    void findBySpecies_returnsEmpty_whenNoOwners() {
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        List<CollectionEntryEntity> result = repository.findBySpecies(species);

        assertTrue(result.isEmpty());
    }

    @Test
    void findBySpecies_doesNotReturnOtherSpeciesEntries() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        List<CollectionEntryEntity> result = repository.findBySpecies(gbb);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByUserAndTerm_returnsMatchingEntries_byScientificName() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        CollectionEntryEntity entry1 = em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user, "Chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(entry1, result.getContent().getFirst());
    }

    @Test
    void searchByUserAndTerm_returnsMatchingEntries_byCommonName() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        gbb.setCommonName("Green Bottle Blue");
        em.persistFlushFind(gbb);
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        CollectionEntryEntity entry1 = em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user, "Green Bottle", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(entry1, result.getContent().getFirst());
    }

    @Test
    void searchByUserAndTerm_isCaseInsensitive() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        CollectionEntryEntity entry = em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user, "chromatopelma", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(entry, result.getContent().getFirst());
    }

    @Test
    void searchByUserAndTerm_doesNotReturnOtherUsersEntries() {
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));

        em.persistFlushFind(TestUtilities.validCollectionEntry(user2, species));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user1, "Chromatopelma", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByUserAndTerm_doesNotReturnOtherUsersEntries_whenMatchingOnCommonName() {
        // Regression test for the Or-precedence bug: a naive derived query
        // (findByUserAndScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase)
        // parses as (user = ?1 AND scientificName LIKE ?2) OR commonName LIKE ?3,
        // which leaks other users' entries whenever the common name matches.
        UserEntity user1 = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        UserEntity user2 = em.persistFlushFind(TestUtilities.verifiedUser("2", "2"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        species.setCommonName("Green Bottle Blue");
        em.persistFlushFind(species);

        em.persistFlushFind(TestUtilities.validCollectionEntry(user2, species));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user1, "Green Bottle", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void searchByUserAndTerm_returnsEmpty_whenNoMatch() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity species = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        em.persistFlushFind(TestUtilities.validCollectionEntry(user, species));

        Page<CollectionEntryEntity> result = repository
                .searchByUserAndTerm(user, "Nonexistent", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}
