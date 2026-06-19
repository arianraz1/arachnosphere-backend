package com.tarantulacommunity.tarantulasocial.features.collection_entry.service;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.mapper.CollectionEntryMapper;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.repository.CollectionEntryRepository;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionEntryService {

    private final CollectionEntryRepository collectionEntryRepository;
    private final CollectionEntryMapper collectionEntryMapper;
    private final UserRepository userRepository;
    private final SpeciesRepository speciesRepository;

    public List<CollectionEntryResponse> getEntriesByUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));

        return collectionEntryRepository.findByUser(user)
                .stream()
                .map(collectionEntryMapper::toResponse)
                .toList();
    }

    public List<CollectionEntryResponse> getEntriesBySpecies(Long speciesId) {
        SpeciesEntity species = speciesRepository.findById(speciesId)
                .orElseThrow(() -> new GlobalExceptionHandler.SpeciesNotFoundException(speciesId));

        return collectionEntryRepository.findBySpecies(species)
                .stream()
                .map(collectionEntryMapper::toResponse)
                .toList();
    }

    public Page<CollectionEntryResponse> searchEntriesByUser(Long userId, String query, Pageable pageable) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));

        return collectionEntryRepository
                .findByUserAndSpecies_ScientificNameContainingIgnoreCase(user, query, pageable)
                .map(collectionEntryMapper::toResponse);
    }

    @Transactional
    public CollectionEntryResponse createEntry(Long userId, CreateCollectionEntryRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new GlobalExceptionHandler.UserNotFoundException(userId));

        SpeciesEntity species = speciesRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new GlobalExceptionHandler.SpeciesNotFoundException(request.getSpeciesId()));

        Optional<CollectionEntryEntity> existing = collectionEntryRepository
                .findByUserAndSpecies(user, species);

        if (existing.isPresent()) {
            return applyUpdate(existing.get(),
                    collectionEntryMapper.toUpdateRequest(existing.get(), request));
        }

        CollectionEntryEntity entry = collectionEntryMapper.toEntity(request);
        entry.setUser(user);
        entry.setSpecies(species);

        return collectionEntryMapper.toResponse(collectionEntryRepository.save(entry));
    }

    @Transactional
    public CollectionEntryResponse updateEntry(Long entryId, Long userId,
                                               UpdateCollectionEntryRequest request) {
        CollectionEntryEntity entry = collectionEntryRepository.findById(entryId)
                .orElseThrow(() -> new GlobalExceptionHandler.CollectionEntryNotFoundException(entryId));

        assertOwnership(entry, userId);

        return applyUpdate(entry, request);
    }

    @Transactional
    public void deleteEntry(Long entryId, Long userId) {
        CollectionEntryEntity entry = collectionEntryRepository.findById(entryId)
                .orElseThrow(() -> new GlobalExceptionHandler.CollectionEntryNotFoundException(entryId));

        assertOwnership(entry, userId);

        collectionEntryRepository.delete(entry);
    }

    private void assertOwnership(CollectionEntryEntity entry, Long userId) {
        if (!entry.getUser().getId().equals(userId)) {
            throw new GlobalExceptionHandler.UnauthorizedException("You do not own this collection entry");
        }
    }

    private CollectionEntryResponse applyUpdate(CollectionEntryEntity entry,
                                                UpdateCollectionEntryRequest request) {
        collectionEntryMapper.updateEntityFromRequest(request, entry);
        return collectionEntryMapper.toResponse(collectionEntryRepository.save(entry));
    }
}
