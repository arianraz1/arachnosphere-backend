package com.tarantulacommunity.tarantulasocial.features.admin.controller;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.SpeciesSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final SpeciesSyncService speciesSyncService;

    @PostMapping("/species/sync")
    public ResponseEntity<String> syncSpecies() {
        log.info("Manual GBIF sync triggered");
        int count = speciesSyncService.syncAllTarantulas();
        return ResponseEntity.ok("Sync complete. " + count + " species saved.");
    }
}