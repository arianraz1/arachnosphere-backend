package com.tarantulacommunity.tarantulasocial.features.collection_entry.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionEntryControllerTest {

    @Mock
    private CollectionEntryService collectionEntryService;

    @InjectMocks
    private CollectionEntryController collectionEntryController;

    @Test
    void getByUser_shouldReturnListOfEntries() {
        CollectionEntryResponse entry = new CollectionEntryResponse();
        entry.setId(1L);
        List<CollectionEntryResponse> entries = List.of(entry);

        when(collectionEntryService.getEntriesByUser(1L)).thenReturn(entries);

        ResponseEntity<List<CollectionEntryResponse>> response = collectionEntryController.getByUser(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst()).isEqualTo(entry);
    }

    @Test
    void getByUser_shouldThrowWhenUserNotFound() {
        when(collectionEntryService.getEntriesByUser(99L))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> collectionEntryController.getByUser(99L)
        );
    }

    @Test
    void searchByUser_shouldReturnPageOfEntries() {
        CollectionEntryResponse entry = new CollectionEntryResponse();
        entry.setId(1L);
        Page<CollectionEntryResponse> page = new PageImpl<>(List.of(entry), PageRequest.of(0, 20), 1);

        when(collectionEntryService.searchEntriesByUser(eq(1L), eq("Chromato"), any(PageRequest.class)))
                .thenReturn(page);

        ResponseEntity<Page<CollectionEntryResponse>> response = collectionEntryController.searchByUser(1L, "Chromato", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(entry);
    }

    @Test
    void searchByUser_shouldThrowWhenUserNotFound() {
        when(collectionEntryService.searchEntriesByUser(eq(99L), eq("query"), any(PageRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> collectionEntryController.searchByUser(99L, "query", PageRequest.of(0, 20))
        );
    }

    @Test
    void getBySpecies_shouldReturnListOfEntries() {
        CollectionEntryResponse entry = new CollectionEntryResponse();
        entry.setId(1L);
        List<CollectionEntryResponse> entries = List.of(entry);

        when(collectionEntryService.getEntriesBySpecies(1L)).thenReturn(entries);

        ResponseEntity<List<CollectionEntryResponse>> response = collectionEntryController.getBySpecies(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getFirst()).isEqualTo(entry);
    }

    @Test
    void getBySpecies_shouldThrowWhenSpeciesNotFound() {
        when(collectionEntryService.getEntriesBySpecies(99L))
                .thenThrow(new GlobalExceptionHandler.SpeciesNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.SpeciesNotFoundException.class,
                () -> collectionEntryController.getBySpecies(99L)
        );
    }

    @Test
    void create_shouldReturnCreatedEntry() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();
        CollectionEntryResponse expectedResponse = new CollectionEntryResponse();
        expectedResponse.setId(1L);

        when(collectionEntryService.createEntry(eq(1L), any(CreateCollectionEntryRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<CollectionEntryResponse> response = collectionEntryController.create(1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void create_shouldThrowWhenUserNotFound() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();

        when(collectionEntryService.createEntry(eq(99L), any(CreateCollectionEntryRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UserNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UserNotFoundException.class,
                () -> collectionEntryController.create(99L, request)
        );
    }

    @Test
    void create_shouldThrowWhenSpeciesNotFound() {
        CreateCollectionEntryRequest request = new CreateCollectionEntryRequest();

        when(collectionEntryService.createEntry(eq(1L), any(CreateCollectionEntryRequest.class)))
                .thenThrow(new GlobalExceptionHandler.SpeciesNotFoundException(1L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.SpeciesNotFoundException.class,
                () -> collectionEntryController.create(1L, request)
        );
    }

    @Test
    void update_shouldReturnUpdatedEntry() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();
        CollectionEntryResponse expectedResponse = new CollectionEntryResponse();
        expectedResponse.setId(1L);

        when(collectionEntryService.updateEntry(eq(1L), eq(1L), any(UpdateCollectionEntryRequest.class)))
                .thenReturn(expectedResponse);

        ResponseEntity<CollectionEntryResponse> response = collectionEntryController.update(1L, 1L, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void update_shouldThrowWhenEntryNotFound() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();

        when(collectionEntryService.updateEntry(eq(99L), eq(1L), any(UpdateCollectionEntryRequest.class)))
                .thenThrow(new GlobalExceptionHandler.CollectionEntryNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.CollectionEntryNotFoundException.class,
                () -> collectionEntryController.update(99L, 1L, request)
        );
    }

    @Test
    void update_shouldThrowWhenUnauthorized() {
        UpdateCollectionEntryRequest request = new UpdateCollectionEntryRequest();

        when(collectionEntryService.updateEntry(eq(1L), eq(99L), any(UpdateCollectionEntryRequest.class)))
                .thenThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this collection entry"));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UnauthorizedException.class,
                () -> collectionEntryController.update(1L, 99L, request)
        );
    }

    @Test
    void delete_shouldReturnNoContent() {
        ResponseEntity<Void> response = collectionEntryController.delete(1L, 1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(collectionEntryService).deleteEntry(1L, 1L);
    }

    @Test
    void delete_shouldThrowWhenEntryNotFound() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.CollectionEntryNotFoundException(99L))
                .when(collectionEntryService).deleteEntry(99L, 1L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.CollectionEntryNotFoundException.class,
                () -> collectionEntryController.delete(99L, 1L)
        );
    }

    @Test
    void delete_shouldThrowWhenUnauthorized() {
        org.mockito.Mockito.doThrow(new GlobalExceptionHandler.UnauthorizedException("You do not own this collection entry"))
                .when(collectionEntryService).deleteEntry(1L, 99L);

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.UnauthorizedException.class,
                () -> collectionEntryController.delete(1L, 99L)
        );
    }

    @Test
    void searchByUser_shouldReturnEmptyPage_whenNoResults() {
        Page<CollectionEntryResponse> emptyPage = Page.empty();
        when(collectionEntryService.searchEntriesByUser(eq(1L), eq("nonexistent"), any(Pageable.class)))
                .thenReturn(emptyPage);

        ResponseEntity<Page<CollectionEntryResponse>> response = collectionEntryController.searchByUser(1L, "nonexistent", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
