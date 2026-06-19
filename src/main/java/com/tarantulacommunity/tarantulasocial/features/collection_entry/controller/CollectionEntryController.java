package com.tarantulacommunity.tarantulasocial.features.collection_entry.controller;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
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

    // ---------- PUBLIC (Nested under User) ----------
    // Returns a simple list (no pagination) — fine for small collections
    @GetMapping("/users/{userId}/collection")
    public ResponseEntity<List<CollectionEntryResponse>> getByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(collectionEntryService.getEntriesByUser(userId));
    }

    // ---------- PUBLIC (Nested under User with Search + Pagination) ----------
    // For large collections or filtered searches — supports pagination
    @GetMapping("/users/{userId}/collection/search")
    public ResponseEntity<Page<CollectionEntryResponse>> searchByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "species.scientificName") Pageable pageable) {

        return ResponseEntity.ok(
                collectionEntryService.searchEntriesByUser(userId, query, pageable));
    }

    // ---------- PUBLIC (Who owns this species?) ----------
    @GetMapping("/collection/species/{speciesId}")
    public ResponseEntity<List<CollectionEntryResponse>> getBySpecies(
            @PathVariable Long speciesId) {

        return ResponseEntity.ok(collectionEntryService.getEntriesBySpecies(speciesId));
    }

    // ---------- PROTECTED (Authenticated user manages their own entries) ----------
    @PostMapping("/collection")
    public ResponseEntity<CollectionEntryResponse> create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateCollectionEntryRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionEntryService.createEntry(userId, request));
    }

    @PutMapping("/collection/{entryId}")
    public ResponseEntity<CollectionEntryResponse> update(
            @PathVariable Long entryId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateCollectionEntryRequest request) {

        return ResponseEntity.ok(
                collectionEntryService.updateEntry(entryId, userId, request));
    }

    @DeleteMapping("/collection/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long entryId,
            @AuthenticationPrincipal Long userId) {

        collectionEntryService.deleteEntry(entryId, userId);
        return ResponseEntity.noContent().build();
    }
}
