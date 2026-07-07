package com.tarantulacommunity.tarantulasocial.features.admin.controller;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.SpeciesSyncService;
import com.tarantulacommunity.tarantulasocial.features.admin.inat.PhotoSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final SpeciesSyncService speciesSyncService;
    private final PhotoSyncService photoSyncService;

    @PostMapping("/species/sync")
    public ResponseEntity<String> syncSpecies() {
        log.info("Manual GBIF sync triggered");
        int count = speciesSyncService.syncAllTarantulas();
        return ResponseEntity.ok("Sync complete. " + count + " species saved.");
    }

    @PostMapping("/species/sync/photos")
    public ResponseEntity<String> syncPhotos() {
        log.info("Manual iNaturalist photo sync triggered");
        photoSyncService.syncPhotos();
        return ResponseEntity.ok("Photo sync complete.");
    }

    @PostMapping("/species/sync/photos/full")
    public ResponseEntity<String> syncAllPhotos() {
        log.info("Manual iNaturalist full photo re-sync triggered");
        photoSyncService.syncAllPhotos();
        return ResponseEntity.ok("Full photo sync complete.");
    }
}