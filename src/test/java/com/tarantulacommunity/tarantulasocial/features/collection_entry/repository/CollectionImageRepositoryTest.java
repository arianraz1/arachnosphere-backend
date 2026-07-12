package com.tarantulacommunity.tarantulasocial.features.collection_entry.repository;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionImageEntity;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class CollectionImageRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CollectionImageRepository repository;

    private CollectionEntryEntity entryA;
    private CollectionEntryEntity entryB;

    @BeforeEach
    void setUp() {
        UserEntity user = em.persistFlushFind(TestUtilities.verifiedUser("1", "1"));
        SpeciesEntity gbb = em.persistFlushFind(TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens"));
        SpeciesEntity bh = em.persistFlushFind(TestUtilities.validSpecies(2L, "Brachypelma hamorii"));

        entryA = em.persistFlushFind(TestUtilities.validCollectionEntry(user, gbb));
        entryB = em.persistFlushFind(TestUtilities.validCollectionEntry(user, bh));
    }

    private CollectionImageEntity image(CollectionEntryEntity entry, String s3Key, int displayOrder) {
        return CollectionImageEntity.builder()
                .collection(entry)
                .s3Key(s3Key)
                .displayOrder(displayOrder)
                .build();
    }

    @Test
    void findByCollectionIdOrderByDisplayOrderAsc_returnsImagesInOrder() {
        em.persistFlushFind(image(entryA, "key-2", 2));
        em.persistFlushFind(image(entryA, "key-0", 0));
        em.persistFlushFind(image(entryA, "key-1", 1));

        List<CollectionImageEntity> result = repository.findByCollectionIdOrderByDisplayOrderAsc(entryA.getId());

        assertEquals(3, result.size());
        assertEquals("key-0", result.get(0).getS3Key());
        assertEquals("key-1", result.get(1).getS3Key());
        assertEquals("key-2", result.get(2).getS3Key());
    }

    @Test
    void findByCollectionIdOrderByDisplayOrderAsc_returnsEmpty_whenNoImages() {
        List<CollectionImageEntity> result = repository.findByCollectionIdOrderByDisplayOrderAsc(entryA.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByCollectionIdInOrderByDisplayOrderAsc_returnsImagesAcrossMultipleEntries() {
        em.persistFlushFind(image(entryA, "a-key-0", 0));
        em.persistFlushFind(image(entryB, "b-key-0", 0));
        em.persistFlushFind(image(entryB, "b-key-1", 1));

        List<CollectionImageEntity> result =
                repository.findByCollectionIdInOrderByDisplayOrderAsc(List.of(entryA.getId(), entryB.getId()));

        assertEquals(3, result.size());
    }

    @Test
    void findByCollectionIdInOrderByDisplayOrderAsc_excludesEntriesNotRequested() {
        em.persistFlushFind(image(entryA, "a-key-0", 0));
        em.persistFlushFind(image(entryB, "b-key-0", 0));

        List<CollectionImageEntity> result =
                repository.findByCollectionIdInOrderByDisplayOrderAsc(List.of(entryA.getId()));

        assertEquals(1, result.size());
        assertEquals("a-key-0", result.getFirst().getS3Key());
    }

    @Test
    void findByIdAndCollectionId_returnsMatch_whenImageBelongsToEntry() {
        CollectionImageEntity saved = em.persistFlushFind(image(entryA, "key-0", 0));

        Optional<CollectionImageEntity> result = repository.findByIdAndCollectionId(saved.getId(), entryA.getId());

        assertTrue(result.isPresent());
        assertEquals(saved.getId(), result.get().getId());
    }

    @Test
    void findByIdAndCollectionId_returnsEmpty_whenImageBelongsToDifferentEntry() {
        CollectionImageEntity saved = em.persistFlushFind(image(entryA, "key-0", 0));

        Optional<CollectionImageEntity> result = repository.findByIdAndCollectionId(saved.getId(), entryB.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void countByCollectionId_countsOnlyThatEntrysImages() {
        em.persistFlushFind(image(entryA, "key-0", 0));
        em.persistFlushFind(image(entryA, "key-1", 1));
        em.persistFlushFind(image(entryB, "key-0", 0));

        assertEquals(2, repository.countByCollectionId(entryA.getId()));
        assertEquals(1, repository.countByCollectionId(entryB.getId()));
    }

    @Test
    void countByCollectionId_returnsZero_whenNoImages() {
        assertEquals(0, repository.countByCollectionId(entryA.getId()));
    }

    @Test
    void findByCollectionIdAndFeaturedTrue_returnsOnlyFeaturedImages() {
        CollectionImageEntity featured = image(entryA, "key-0", 0);
        featured.setFeatured(true);
        em.persistFlushFind(featured);
        em.persistFlushFind(image(entryA, "key-1", 1));

        List<CollectionImageEntity> result = repository.findByCollectionIdAndFeaturedTrue(entryA.getId());

        assertEquals(1, result.size());
        assertEquals("key-0", result.getFirst().getS3Key());
    }

    @Test
    void findByCollectionIdAndFeaturedTrue_returnsEmpty_whenNoneFeatured() {
        em.persistFlushFind(image(entryA, "key-0", 0));

        List<CollectionImageEntity> result = repository.findByCollectionIdAndFeaturedTrue(entryA.getId());

        assertTrue(result.isEmpty());
    }
}
