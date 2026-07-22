package com.tarantulacommunity.tarantulasocial.features.species.controller;

import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesDetailResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.species.service.SpeciesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpeciesControllerTest {

    @Mock
    private SpeciesService speciesService;

    @Mock
    private CollectionEntryService collectionEntryService;

    @Mock
    private SpeciesMapper speciesMapper;

    @InjectMocks
    private SpeciesController speciesController;

    @Test
    void getById_shouldReturnSpeciesResponse() {
        SpeciesResponse expectedResponse = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null, null);
        when(speciesService.getSpeciesById(1L)).thenReturn(expectedResponse);

        ResponseEntity<SpeciesResponse> response = speciesController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
    }

    @Test
    void getById_shouldThrowWhenNotFound() {
        when(speciesService.getSpeciesById(99L))
                .thenThrow(new GlobalExceptionHandler.SpeciesNotFoundException(99L));

        org.junit.jupiter.api.Assertions.assertThrows(
                GlobalExceptionHandler.SpeciesNotFoundException.class,
                () -> speciesController.getById(99L)
        );
    }

    @Test
    void getOwnersBySpecies_shouldReturnPaginatedOwners() {
        CollectionEntryOwnerResponse owner1 = new CollectionEntryOwnerResponse();
        owner1.setId(1L);
        CollectionEntryOwnerResponse owner2 = new CollectionEntryOwnerResponse();
        owner2.setId(2L);

        Page<CollectionEntryOwnerResponse> page = new PageImpl<>(List.of(owner1, owner2), PageRequest.of(0, 2), 10);

        when(collectionEntryService.getOwnersBySpecies(1L, PageRequest.of(0, 2))).thenReturn(page);

        ResponseEntity<Page<CollectionEntryOwnerResponse>> response = speciesController.getOwnersBySpecies(1L, PageRequest.of(0, 2));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(2);
        assertThat(response.getBody().getTotalElements()).isEqualTo(10);
    }

    @Test
    void searchOwned_shouldReturnPageOfSpeciesSummaries() {
        SpeciesSummaryResponse summary = new SpeciesSummaryResponse(1L, "Chromatopelma cyaneopubescens", null, null, null);
        Page<SpeciesSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);

        when(speciesService.searchOwnedSpecies(eq("Chromato"), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchOwned("Chromato", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(summary);
    }

    @Test
    void searchUnowned_shouldReturnPageOfSpeciesSummaries() {
        SpeciesSummaryResponse summary = new SpeciesSummaryResponse(2L, "Brachypelma hamorii", null, null, null);
        Page<SpeciesSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);

        when(speciesService.searchUnownedSpecies(eq("Brachy"), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchUnowned("Brachy", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(summary);
    }

    @Test
    void searchAll_shouldReturnPageOfSpeciesSummaries() {
        SpeciesSummaryResponse summary = new SpeciesSummaryResponse(1L, "Chromatopelma cyaneopubescens", null, null, null);
        Page<SpeciesSummaryResponse> page = new PageImpl<>(List.of(summary), PageRequest.of(0, 20), 1);

        when(speciesService.searchAllSpecies(eq("Chromato"), any(PageRequest.class))).thenReturn(page);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchAll("Chromato", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().getContent().getFirst()).isEqualTo(summary);
    }

    @Test
    void searchAll_shouldReturnEmptyPageWhenNoResults() {
        Page<SpeciesSummaryResponse> emptyPage = Page.empty();

        when(speciesService.searchAllSpecies(eq("nonexistent"), any(PageRequest.class))).thenReturn(emptyPage);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchAll("nonexistent", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void searchOwned_shouldReturnEmptyPageWhenNoResults() {
        Page<SpeciesSummaryResponse> emptyPage = Page.empty();

        when(speciesService.searchOwnedSpecies(eq("nonexistent"), any(PageRequest.class))).thenReturn(emptyPage);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchOwned("nonexistent", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void searchUnowned_shouldReturnEmptyPageWhenNoResults() {
        Page<SpeciesSummaryResponse> emptyPage = Page.empty();

        when(speciesService.searchUnownedSpecies(eq("nonexistent"), any(PageRequest.class))).thenReturn(emptyPage);

        ResponseEntity<Page<SpeciesSummaryResponse>> response = speciesController.searchUnowned("nonexistent", PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void getOwnersBySpecies_shouldReturnEmptyPage() {
        Page<CollectionEntryOwnerResponse> empty = Page.empty();
        when(collectionEntryService.getOwnersBySpecies(1L, PageRequest.of(0, 20))).thenReturn(empty);

        ResponseEntity<Page<CollectionEntryOwnerResponse>> response = speciesController.getOwnersBySpecies(1L, PageRequest.of(0, 20));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}
