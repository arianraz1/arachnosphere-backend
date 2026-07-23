package com.tarantulacommunity.tarantulasocial.features.collection_entry.service;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionImageResponse;
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
import java.util.Map;
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

    @Mock
    private CollectionImageService collectionImageService;

    @InjectMocks
    private CollectionEntryService collectionEntryService;

    private UserEntity user;
    private SpeciesEntity species;
    private CollectionEntryEntity entry;
    private CollectionEntryResponse entryResponse;

    @BeforeEach
    void setUp() {
        user = TestUtilities.verifiedUser("1", "1");
        user.setId(1L);

        species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        species.setId(1L);

        entry = TestUtilities.validCollectionEntry(user, species);
        entry.setId(1L);

        entryResponse = new CollectionEntryResponse();
        entryResponse.setId(1L);
    }

    // Every path returning a CollectionEntryResponse/OwnerResponse now also populates
    // images via collectionImageService, so existing tests stub that dependency too.

    @Test
    void getEntriesByUser_returnsPageOfEntries() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);
        when(collectionEntryRepository.findByUser(user, PageRequest.of(0, 20))).thenReturn(page);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L))).thenReturn(Map.of());
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        Page<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L, PageRequest.of(0, 20));

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getEntriesByUser_throwsUserNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.UserNotFoundException.class,
                () -> collectionEntryService.getEntriesByUser(99L, PageRequest.of(0, 20)));
    }

    @Test
    void getEntriesByUser_returnsEmpty_whenNoEntries() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(collectionEntryRepository.findByUser(user, PageRequest.of(0, 20))).thenReturn(page);
        when(collectionImageService.listImagesByCollectionIds(List.of())).thenReturn(Map.of());

        Page<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L, PageRequest.of(0, 20));

        assertTrue(result.isEmpty());
    }

    @Test
    void getEntriesByUser_populatesImagesFromBatchLookup() {
        CollectionImageResponse image = new CollectionImageResponse(100L, "https://cdn/img.jpg", "cap", true, 0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);
        when(collectionEntryRepository.findByUser(user, PageRequest.of(0, 20))).thenReturn(page);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(image)));
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        Page<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L, PageRequest.of(0, 20));

        assertEquals(List.of(image), result.getContent().getFirst().getImages());
        verify(collectionImageService, never()).listImages(anyLong());
    }

    @Test
    void getEntriesByUser_entryWithNoImages_defaultsToEmptyList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);
        when(collectionEntryRepository.findByUser(user, PageRequest.of(0, 20))).thenReturn(page);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L))).thenReturn(Map.of());
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);

        Page<CollectionEntryResponse> result = collectionEntryService.getEntriesByUser(1L, PageRequest.of(0, 20));

        assertNotNull(result.getContent().getFirst().getImages());
        assertTrue(result.getContent().getFirst().getImages().isEmpty());
    }

    @Test
    void searchEntriesByUser_returnsMatchingEntries() {
        Page<CollectionEntryEntity> page = new PageImpl<>(List.of(entry));

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(collectionEntryRepository.searchByUserAndTerm(
                user, "Chromato", PageRequest.of(0, 10))).thenReturn(page);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L))).thenReturn(Map.of());

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
        when(collectionImageService.listImagesByCollectionIds(List.of(1L))).thenReturn(Map.of());

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
        when(collectionImageService.listImagesByCollectionIds(List.of())).thenReturn(Map.of());

        List<CollectionEntryResponse> result = collectionEntryService.getEntriesBySpecies(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void getEntryById_returnsEntry() {
        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);
        when(collectionImageService.listImages(1L)).thenReturn(List.of());

        CollectionEntryResponse result = collectionEntryService.getEntryById(1L);

        assertEquals(entryResponse, result);
    }

    @Test
    void getEntryById_throwsCollectionEntryNotFoundException_whenNotFound() {
        when(collectionEntryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.CollectionEntryNotFoundException.class, () ->
                collectionEntryService.getEntryById(99L));
    }

    @Test
    void getEntryById_populatesImages() {
        CollectionImageResponse image = new CollectionImageResponse(100L, "https://cdn/img.jpg", "cap", true, 0);

        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);
        when(collectionImageService.listImages(1L)).thenReturn(List.of(image));

        CollectionEntryResponse result = collectionEntryService.getEntryById(1L);

        assertEquals(List.of(image), result.getImages());
    }

    // ---------- CREATE (INSERT) ----------
    @Test
    void createEntry_savesAndReturnsEntryResult_whenNoDuplicate() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setAdultMale(1);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findByUserAndSpecies(user, species)).thenReturn(Optional.empty());
        when(collectionEntryMapper.toEntity(request)).thenReturn(entry);
        when(collectionEntryRepository.save(entry)).thenReturn(entry);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);
        when(collectionImageService.listImages(1L)).thenReturn(List.of());

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
        when(collectionImageService.listImages(1L)).thenReturn(List.of());

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
    void createEntry_throwsBadRequestException_whenAllTarantulaCountsAreZero() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        request.setSpeciesId(1L);
        request.setSlingMale(0);
        request.setSlingFemale(0);
        request.setSlingUnsexed(0);
        request.setJuvenileMale(0);
        request.setJuvenileFemale(0);
        request.setJuvenileUnsexed(0);
        request.setSubadultMale(0);
        request.setSubadultFemale(0);
        request.setSubadultUnsexed(0);
        request.setAdultMale(0);
        request.setAdultFemale(0);
        request.setAdultUnsexed(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));

        assertThrows(GlobalExceptionHandler.BadRequestException.class, () ->
                collectionEntryService.createEntry(1L, request));
    }

    @Test
    void updateEntry_updatesAndReturnsResponse() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setAdultMale(3);

        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));
        when(collectionEntryRepository.save(entry)).thenReturn(entry);
        when(collectionEntryMapper.toResponse(entry)).thenReturn(entryResponse);
        when(collectionImageService.listImages(1L)).thenReturn(List.of());

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
    void updateEntry_throwsBadRequestException_whenAllTarantulaCountsAreZero() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        request.setSlingMale(0);
        request.setSlingFemale(0);
        request.setSlingUnsexed(0);
        request.setJuvenileMale(0);
        request.setJuvenileFemale(0);
        request.setJuvenileUnsexed(0);
        request.setSubadultMale(0);
        request.setSubadultFemale(0);
        request.setSubadultUnsexed(0);
        request.setAdultMale(0);
        request.setAdultFemale(0);
        request.setAdultUnsexed(0);

        when(collectionEntryRepository.findById(1L)).thenReturn(Optional.of(entry));

        assertThrows(GlobalExceptionHandler.BadRequestException.class, () ->
                collectionEntryService.updateEntry(1L, 1L, request));
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

    @Test
    void getOwnersBySpecies_returnsOwnerDtos() {
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findBySpecies(species)).thenReturn(List.of(entry));

        CollectionEntryOwnerResponse ownerResponse = new CollectionEntryOwnerResponse();
        ownerResponse.setId(1L);
        when(collectionEntryMapper.toOwnerResponse(entry)).thenReturn(ownerResponse);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L))).thenReturn(Map.of());

        List<CollectionEntryOwnerResponse> result = collectionEntryService.getOwnersBySpecies(1L);

        assertEquals(1, result.size());
        assertEquals(ownerResponse, result.getFirst());
    }

    @Test
    void getOwnersBySpecies_populatesImagesFromBatchLookup() {
        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(collectionEntryRepository.findBySpecies(species)).thenReturn(List.of(entry));

        CollectionEntryOwnerResponse ownerResponse = new CollectionEntryOwnerResponse();
        when(collectionEntryMapper.toOwnerResponse(entry)).thenReturn(ownerResponse);

        CollectionImageResponse image = new CollectionImageResponse(100L, "https://cdn/img.jpg", null, false, 0);
        when(collectionImageService.listImagesByCollectionIds(List.of(1L)))
                .thenReturn(Map.of(1L, List.of(image)));

        List<CollectionEntryOwnerResponse> result = collectionEntryService.getOwnersBySpecies(1L);

        assertEquals(List.of(image), result.getFirst().getImages());
    }

    @Test
    void getOwnersBySpecies_throwsSpeciesNotFoundException_whenSpeciesNotFound() {
        when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.SpeciesNotFoundException.class,
                () -> collectionEntryService.getOwnersBySpecies(99L));
    }
}