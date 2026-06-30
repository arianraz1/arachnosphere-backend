package com.tarantulacommunity.tarantulasocial.features.collection_entry.service;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionEntryServiceTest {

    @Mock
    private CollectionEntryRepository collectionEntryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpeciesRepository speciesRepository;

    @Mock
    private CollectionEntryMapper collectionEntryMapper;

    @InjectMocks
    private CollectionEntryService collectionEntryService;

    private UserEntity user;
    private SpeciesEntity species;
    private CollectionEntryEntity entry;
    private CollectionEntryResponse entryResponse;

    @BeforeEach
    void setUp() {
        user = TestUtilities.validUser("1", "1");
        user.setId(1L);

        species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        species.setId(1L);

        entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(1L);

        entryResponse = new CollectionEntryResponse();
        entryResponse.setId(1L);
    }

    @Test
    void getEntriesByUser_returnsAllEntries() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(collectionEntryRepository.findByUser(user)).thenReturn(List.of(entry));
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        List<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L);

        assertEquals(1, result.size());
        assertEquals(entryResponse, result.getFirst());
    }

    @Test
    void getEntriesByUser_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () -> collectionEntryService.getEntriesByUser(99L));
    }

    @Test
    void getEntriesByUser_returnsEmpty_whenNoEntries() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(collectionEntryRepository.findByUser(user)).thenReturn(List.of());

        List<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchEntriesByUser_returnsMatchingEntries() {
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(entry));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(collectionEntryRepository.findByUserAndSpecies_ScientificNameContainingIgnoreCase(
                user, "Chromato", PageRequest.of(0, 10))).thenReturn(page);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        Page<CollectionEntryResponse> result = collectionEntryService
                .searchEntriesByUser(1L, "Chromato", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(entryResponse, result.getContent().getFirst());
    }

    @Test
    void searchEntriesByUser_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                collectionEntryService.searchEntriesByUser(99L, "Chromato", PageRequest.of(0, 10)));
    }

    @Test
    void getEntriesBySpecies_returnsAllOwners() {
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findBySpecies(species)).thenReturn(List.of(entry));
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        List<CollectionEntryResponse> result = collectionEntryService.getEntriesBySpecies(1L);

        assertEquals(1, result.size());
        assertEquals(entryResponse, result.getFirst());
    }

    @Test
    void getEntriesBySpecies_throwsSpeciesNotFoundException_whenSpeciesNotFound() {
        when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.SpeciesNotFoundException.class, () -> collectionEntryService.getEntriesBySpecies(99L));
    }

    @Test
    void getEntriesBySpecies_returnsEmpty_whenNoOwners() {
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findBySpecies(species)).thenReturn(List.of());

        List<CollectionEntryResponse> result = collectionEntryService.getEntriesBySpecies(1L);

        assertTrue(result.isEmpty());
    }

    // ---------- CREATE (INSERT) ----------
    @Test
    void createEntry_savesAndReturnsEntryResult_whenNoDuplicate() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findByUserAndSpecies(user, species)).thenReturn(Optional.empty());
        when(collectionEntryMapper.toEntity(request)).thenReturn(entry);
        when(collectionEntryRepository.save(entry)).thenReturn(entry);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        CollectionEntryService.EntryResult result = collectionEntryService.createEntry(1L, request);

        assertEquals(entryResponse, result.entry());
        assertTrue(result.created(), "Entry should be marked as created (insert)");
        verify(collectionEntryRepository).save(entry);
    }

    // ---------- CREATE (UPDATE) ----------
    @Test
    void createEntry_updatesExistingAndReturnsEntryResult_whenDuplicateFound() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setAdultMale(2);

        UpdateCollectionEntryRequest updateRequest = new UpdateCollectionEntryRequest();
        updateRequest.setAdultMale(2);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findByUserAndSpecies(user, species)).thenReturn(Optional.of(entry));
        when(collectionEntryMapper.toUpdateRequest(entry, request)).thenReturn(updateRequest);
        when(collectionEntryRepository.save(entry)).thenReturn(entry);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        CollectionEntryService.EntryResult result = collectionEntryService.createEntry(1L, request);

        assertEquals(entryResponse, result.entry());
        assertFalse(result.created(), "Entry should be marked as not created (update)");
        verify(collectionEntryRepository).save(entry);
    }

    @Test
    void createEntry_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class, () ->
                collectionEntryService.createEntry(99L, new CreateCollectionEntryRequest()));
    }

    @Test
    void createEntry_throwsSpeciesNotFoundException_whenSpeciesNotFound() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(99L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.SpeciesNotFoundException.class, () ->
                collectionEntryService.createEntry(1L, request));
    }

    @Test
    void updateEntry_updatesAndReturnsResponse() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setAdultMale(3);

        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(collectionEntryRepository.save(entry)).thenReturn(entry);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        CollectionEntryResponse result = collectionEntryService.updateEntry(1L, 1L, request);

        assertEquals(entryResponse, result);
        verify(collectionEntryMapper).updateEntityFromRequest(request, entry);
        verify(collectionEntryRepository).save(entry);
    }

    @Test
    void updateEntry_throwsCollectionEntryNotFoundException_whenNotFound() {
        when(collectionEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.CollectionEntryNotFoundException.class, () ->
                collectionEntryService.updateEntry(99L, 1L, new UpdateCollectionEntryRequest()));
    }

    @Test
    void updateEntry_throwsUnauthorizedException_whenUserDoesNotOwnEntry() {
        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class, () ->
                collectionEntryService.updateEntry(1L, 99L, new UpdateCollectionEntryRequest()));
    }

    @Test
    void deleteEntry_deletesEntry() {
        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

        collectionEntryService.deleteEntry(1L, 1L);

        verify(collectionEntryRepository).delete(entry);
    }

    @Test
    void deleteEntry_throwsCollectionEntryNotFoundException_whenNotFound() {
        when(collectionEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.CollectionEntryNotFoundException.class, () ->
                collectionEntryService.deleteEntry(99L, 1L));
    }

    @Test
    void deleteEntry_throwsUnauthorizedException_whenUserDoesNotOwnEntry() {
        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

        assertThrows(GlobalExceptionHandler.UnauthorizedException.class, () ->
                collectionEntryService.deleteEntry(1L, 99L));
    }
}