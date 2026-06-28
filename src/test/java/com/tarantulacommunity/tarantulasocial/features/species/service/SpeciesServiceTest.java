package com.tarantulacommunity.tarantulasocial.features.species.service;

import com.tarantulacommunity.tarantulasocial.TestUtilities;
import com.tarantulacommunity.tarantulasocial.exception.GlobalExceptionHandler;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.species.repository.SpeciesRepository;
import com.tarantulacommunity.tarantulasocial.features.species.service.SpeciesService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpeciesServiceTest {

    @Mock
    private SpeciesRepository speciesRepository;

    @Mock
    private SpeciesMapper speciesMapper;

    @InjectMocks
    private SpeciesService speciesService;

    @Test
    void getSpeciesById_returnsSpeciesResponse() {
        SpeciesEntity species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        species.setId(1L);

        SpeciesResponse expectedResponse = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null);

        when(speciesRepository.findById(1L)).thenReturn(Optional.of(species));
        when(speciesMapper.toResponse(species)).thenReturn(expectedResponse);

        SpeciesResponse result = speciesService.getSpeciesById(1L);

        assertEquals(expectedResponse, result);
    }

    @Test
    void getSpeciesById_throwsSpeciesNotFoundException_whenNotFound() {
        when(speciesRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.SpeciesNotFoundException.class, () -> speciesService.getSpeciesById(99L));
    }

    @Test
    void getSpeciesByNubGbifKey_returnsSpeciesResponse() {
        SpeciesEntity species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");

        SpeciesResponse expectedResponse = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", null, null, null);

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(species));
        when(speciesMapper.toResponse(species)).thenReturn(expectedResponse);

        SpeciesResponse result = speciesService.getSpeciesByGbifNubKey(1L);

        assertEquals(expectedResponse, result);
    }

    @Test
    void getSpeciesByNubGbifKey_throwsSpeciesNotFoundException_whenNotFound() {
        when(speciesRepository.findByGbifNubKey(99L)).thenReturn(Optional.empty());

        assertThrows(GlobalExceptionHandler.SpeciesNotFoundException.class, () -> speciesService.getSpeciesByGbifNubKey(99L));
    }

    @Test
    void searchUnownedSpecies_returnsPageOfSummaryResponses() {
        SpeciesEntity species = TestUtilities.validSpecies(2L, "Brachypelma hamorii");
        SpeciesSummaryResponse summaryResponse = new SpeciesSummaryResponse(2L, "Brachypelma hamorii", null, null);

        Page<SpeciesEntity> page = new PageImpl<>(List.of(species));

        when(speciesRepository.findUnownedSpeciesByQuery("Brachy", PageRequest.of(0, 10))).thenReturn(page);
        when(speciesMapper.toSummaryResponse(species)).thenReturn(summaryResponse);

        Page<SpeciesSummaryResponse> result = speciesService.searchUnownedSpecies("Brachy", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(summaryResponse, result.getContent().getFirst());
    }

    @Test
    void searchUnownedSpecies_returnsEmpty_whenNoMatch() {
        when(speciesRepository.findUnownedSpeciesByQuery("nonexistent", PageRequest.of(0, 10)))
                .thenReturn(Page.empty());

        Page<SpeciesSummaryResponse> result = speciesService.searchUnownedSpecies("nonexistent", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void saveFromGbif_savesAndReturnsResponse_whenSpeciesDoesNotExist() {
        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.empty());

        SpeciesEntity savedSpecies = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        SpeciesResponse expectedResponse = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", "GBB", null, null);

        when(speciesRepository.save(any(SpeciesEntity.class))).thenReturn(savedSpecies);
        when(speciesMapper.toResponse(savedSpecies)).thenReturn(expectedResponse);

        SpeciesResponse result = speciesService.saveFromGbif(1L, "Chromatopelma cyaneopubescens", "GBB", null, null);

        assertEquals(expectedResponse, result);
        verify(speciesRepository).save(any(SpeciesEntity.class));
    }

    @Test
    void saveFromGbif_returnsExistingSpecies_whenAlreadyExists() {
        SpeciesEntity existingSpecies = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        SpeciesResponse expectedResponse = new SpeciesResponse(1L, 1L, "Chromatopelma cyaneopubescens", "GBB", null, null);

        when(speciesRepository.findByGbifNubKey(1L)).thenReturn(Optional.of(existingSpecies));
        when(speciesMapper.toResponse(existingSpecies)).thenReturn(expectedResponse);

        SpeciesResponse result = speciesService.saveFromGbif(1L, "Chromatopelma cyaneopubescens", "GBB", null, null);

        assertEquals(expectedResponse, result);
        verify(speciesRepository, never()).save(any());
    }

    @Test
    void searchOwnedSpecies_callsOrderByOwnerCountQuery() {
        SpeciesEntity species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        SpeciesSummaryResponse summaryResponse = new SpeciesSummaryResponse(1L, "Chromatopelma cyaneopubescens", null, null);

        Page<SpeciesEntity> page = new PageImpl<>(List.of(species));

        when(speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("Chromato", PageRequest.of(0, 10))).thenReturn(page);
        when(speciesMapper.toSummaryResponse(species)).thenReturn(summaryResponse);

        Page<SpeciesSummaryResponse> result = speciesService.searchOwnedSpecies("Chromato", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        verify(speciesRepository).findOwnedSpeciesByQueryOrderByOwnerCount("Chromato", PageRequest.of(0, 10));
        verify(speciesRepository, never()).findOwnedSpeciesByQuery(any(), any());
    }

    @Test
    void searchOwnedSpecies_returnsPageOfSummaryResponses() {
        SpeciesEntity species = TestUtilities.validSpecies(1L, "Chromatopelma cyaneopubescens");
        SpeciesSummaryResponse summaryResponse = new SpeciesSummaryResponse(1L, "Chromatopelma cyaneopubescens", null, null);

        Page<SpeciesEntity> page = new PageImpl<>(List.of(species));

        when(speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("Chromato", PageRequest.of(0, 10))).thenReturn(page);
        when(speciesMapper.toSummaryResponse(species)).thenReturn(summaryResponse);

        Page<SpeciesSummaryResponse> result = speciesService.searchOwnedSpecies("Chromato", PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals(summaryResponse, result.getContent().getFirst());
    }

    @Test
    void searchOwnedSpecies_returnsEmpty_whenNoMatch() {
        when(speciesRepository.findOwnedSpeciesByQueryOrderByOwnerCount("nonexistent", PageRequest.of(0, 10)))
                .thenReturn(Page.empty());

        Page<SpeciesSummaryResponse> result = speciesService.searchOwnedSpecies("nonexistent", PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }
}