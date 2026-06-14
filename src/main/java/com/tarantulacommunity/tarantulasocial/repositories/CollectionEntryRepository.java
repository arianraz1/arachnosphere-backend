package com.tarantulacommunity.tarantulasocial.repositories;

import com.tarantulacommunity.tarantulasocial.domain.entities.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.domain.entities.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.domain.entities.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionEntryRepository extends JpaRepository<CollectionEntryEntity, Long> {

    // profile page: full collection
    List<CollectionEntryEntity> findByUser(UserEntity user);

    // profile page: searchable collection
    Page<CollectionEntryEntity> findByUserAndSpecies_ScientificNameContainingIgnoreCase(
            UserEntity user, String scientificName, Pageable pageable);

    // species detail page: list of owners
    List<CollectionEntryEntity> findBySpecies(SpeciesEntity species);

    // duplicate check before create
    Optional<CollectionEntryEntity> findByUserAndSpecies(UserEntity user, SpeciesEntity species);

    boolean existsByUserAndSpecies(UserEntity user, SpeciesEntity species);
}
