package com.tarantulacommunity.tarantulasocial.features.species.controller;


import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryOwnerResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesDetailResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesResponse;
import com.tarantulacommunity.tarantulasocial.features.species.dto.response.SpeciesSummaryResponse;
import com.tarantulacommunity.tarantulasocial.features.species.mapper.SpeciesMapper;
import com.tarantulacommunity.tarantulasocial.features.species.service.SpeciesService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/species")
@RequiredArgsConstructor
public class SpeciesController {

    private final SpeciesService speciesService;
    private final CollectionEntryService collectionEntryService;
    private final SpeciesMapper speciesMapper;

    // ---------- DETAIL PAGES ----------
    @GetMapping("/{id}")
    public ResponseEntity<SpeciesResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(speciesService.getSpeciesById(id));
    }

    @GetMapping("/{id}/detail")
    public ResponseEntity<SpeciesDetailResponse> getDetail(@PathVariable Long id) {
        SpeciesResponse species = speciesService.getSpeciesById(id);
        List<CollectionEntryOwnerResponse> owners = collectionEntryService.getOwnersBySpecies(id);
        return ResponseEntity.ok(speciesMapper.toDetailResponse(species, owners));
    }

    // ---------- HOME PAGE SEARCH (parallel-friendly) ----------
    @GetMapping("/search/owned")
    public ResponseEntity<Page<SpeciesSummaryResponse>> searchOwned(
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(speciesService.searchOwnedSpecies(query, pageable));
    }

    @GetMapping("/search/unowned")
    public ResponseEntity<Page<SpeciesSummaryResponse>> searchUnowned(
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "scientificName") Pageable pageable) {
        return ResponseEntity.ok(speciesService.searchUnownedSpecies(query, pageable));
    }

    // ---------- DROPDOWN / FORM SEARCH ----------
    @GetMapping("/search/all")
    public ResponseEntity<Page<SpeciesSummaryResponse>> searchAll(
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "scientificName") Pageable pageable) {
        return ResponseEntity.ok(speciesService.searchAllSpecies(query, pageable));
    }
}