package com.tarantulacommunity.tarantulasocial.features.collection_entry.repository;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionEntryRepository extends JpaRepository<CollectionEntryEntity, Long> {

    // profile page: paginated collection
    Page<CollectionEntryEntity> findByUser(UserEntity user, Pageable pageable);

    // profile page: searchable collection
    @Query("SELECT c FROM CollectionEntryEntity c " +
            "WHERE c.user = :user " +
            "AND (LOWER(c.species.scientificName) LIKE LOWER(CONCAT('%', :term, '%')) " +
            "OR LOWER(c.species.commonName) LIKE LOWER(CONCAT('%', :term, '%')))")
    Page<CollectionEntryEntity> searchByUserAndTerm(
            @Param("user") UserEntity user,
            @Param("term") String term,
            Pageable pageable);

    // species detail page: list of owners
    List<CollectionEntryEntity> findBySpecies(SpeciesEntity species);

    // species detail page: paginated list of owners
    Page<CollectionEntryEntity> findBySpecies(SpeciesEntity species, Pageable pageable);

    // duplicate check before create
    Optional<CollectionEntryEntity> findByUserAndSpecies(UserEntity user, SpeciesEntity species);

    boolean existsByUserAndSpecies(UserEntity user, SpeciesEntity species);

    boolean existsByIdAndUserId(Long collectionId, Long userId);
}
