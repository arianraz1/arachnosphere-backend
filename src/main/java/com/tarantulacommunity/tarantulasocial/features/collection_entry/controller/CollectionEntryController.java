package com.tarantulacommunity.tarantulasocial.features.collection_entry.controller;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.CreateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.request.UpdateCollectionEntryRequest;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionEntryResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.dto.response.CollectionImageResponse;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionEntryService;
import com.tarantulacommunity.tarantulasocial.features.collection_entry.service.CollectionImageService;
import com.tarantulacommunity.tarantulasocial.security.auth.model.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CollectionEntryController {

    private final CollectionEntryService collectionEntryService;
    private final CollectionImageService collectionImageService;

    @GetMapping("/users/{userId}/collection")
    public ResponseEntity<List<CollectionEntryResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(collectionEntryService.getEntriesByUser(userId));
    }

    @GetMapping("/users/{userId}/collection/search")
    public ResponseEntity<Page<CollectionEntryResponse>> searchByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "") String query,
            @PageableDefault(size = 20, sort = "species.scientificName") Pageable pageable) {
        return ResponseEntity.ok(collectionEntryService.searchEntriesByUser(userId, query, pageable));
    }

    @GetMapping("/collection/species/{speciesId}")
    public ResponseEntity<List<CollectionEntryResponse>> getBySpecies(@PathVariable Long speciesId) {
        return ResponseEntity.ok(collectionEntryService.getEntriesBySpecies(speciesId));
    }

    @GetMapping("/collection/{entryId}/images")
    public ResponseEntity<List<CollectionImageResponse>> listImages(@PathVariable Long entryId) {
        return ResponseEntity.ok(collectionImageService.listImages(entryId));
    }

    @PostMapping("/collection")
    public ResponseEntity<CollectionEntryResponse> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCollectionEntryRequest request) {
        CollectionEntryService.EntryResult result = collectionEntryService.createEntry(userDetails.getId(), request);
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(result.entry());
    }

    @PutMapping("/collection/{entryId}")
    public ResponseEntity<CollectionEntryResponse> update(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateCollectionEntryRequest request) {
        return ResponseEntity.ok(collectionEntryService.updateEntry(entryId, userDetails.getId(), request));
    }

    @DeleteMapping("/collection/{entryId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long entryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        collectionEntryService.deleteEntry(entryId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/collection/{entryId}/images")
    public ResponseEntity<CollectionImageResponse> uploadImage(
            @PathVariable Long entryId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        CollectionImageResponse response = collectionImageService.uploadImage(userDetails.getId(), entryId, file, caption);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/collection/{entryId}/images/{imageId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long entryId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        collectionImageService.deleteImage(userDetails.getId(), entryId, imageId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/collection/{entryId}/images/{imageId}/featured")
    public ResponseEntity<CollectionImageResponse> setFeatured(
            @PathVariable Long entryId,
            @PathVariable Long imageId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(collectionImageService.setFeatured(userDetails.getId(), entryId, imageId));
    }
}