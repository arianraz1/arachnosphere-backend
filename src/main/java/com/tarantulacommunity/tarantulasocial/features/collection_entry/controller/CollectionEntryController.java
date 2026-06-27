package com.tarantulacommunity.tarantulasocial.features.collection_entry.controller;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
import com.tarantulacommunity.tarantulasocial.security.dto.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CollectionEntryController {

    private final CollectionEntryService collectionEntryService;

    // ---------- PUBLIC ----------

    @GetMapping("/users/{userId}/collection")
    public ResponseEntity<List<CollectionEntryResponse>> getByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(collectionEntryService.getEntriesByUser(userId));
    }

    @GetMapping("/users/{userId}/collection/search")
    public ResponseEntity<Page<CollectionEntryResponse>> searchByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "species.scientificName") Pageable pageable) {
        return ResponseEntity.ok(
                collectionEntryService.searchEntriesByUser(userId, query, pageable));
    }

    @GetMapping("/collection/species/{speciesId}")
    public ResponseEntity<List<CollectionEntryResponse>> getBySpecies(
            @PathVariable Long speciesId) {
        return ResponseEntity.ok(collectionEntryService.getEntriesBySpecies(speciesId));
    }

    // ---------- PROTECTED ----------

    @PostMapping("/collection")
    public ResponseEntity<CollectionEntryResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCollectionEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionEntryService.createEntry(userDetails.getId(), request));
    }

    @PutMapping("/collection/{entryId}")
    public ResponseEntity<CollectionEntryResponse> update(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCollectionEntryRequest request) {
        return ResponseEntity.ok(
                collectionEntryService.updateEntry(entryId, userDetails.getId(), request));
    }

    @DeleteMapping("/collection/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        collectionEntryService.deleteEntry(entryId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}