package com.tarantulacommunity.tarantulasocial.features.species.repository;

import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeciesRepository extends JpaRepository<SpeciesEntity, Long> {

    Optional<SpeciesEntity> findByGbifNubKey(Long gbifNubKey);

    Optional<SpeciesEntity> findByScientificName(String scientificName);

    // used by both home page sections with the same search term
    Page<SpeciesEntity> findByScientificNameContainingIgnoreCaseOrCommonNameContainingIgnoreCase(
            String scientificName, String commonName, Pageable pageable);

    // home page: owned species section with search
    @Query("SELECT s FROM SpeciesEntity s WHERE EXISTS " +
            "(SELECT c FROM CollectionEntryEntity c WHERE c.species = s) " +
            "AND (LOWER(s.scientificName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.commonName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SpeciesEntity> findOwnedSpeciesByQuery(String query, Pageable pageable);

    // home page: unowned species section with search
    @Query("SELECT s FROM SpeciesEntity s WHERE NOT EXISTS " +
            "(SELECT c FROM CollectionEntryEntity c WHERE c.species = s) " +
            "AND (LOWER(s.scientificName) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.commonName) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<SpeciesEntity> findUnownedSpeciesByQuery(String query, Pageable pageable);
}
