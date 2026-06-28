package com.tarantulacommunity.tarantulasocial.features.admin.controller;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.SpeciesSyncService;
import com.tarantulacommunity.tarantulasocial.features.admin.inat.PhotoSyncService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private SpeciesSyncService speciesSyncService;

    @Mock
    private PhotoSyncService photoSyncService;

    @InjectMocks
    private AdminController adminController;

    @Test
    void syncSpecies_shouldReturnSuccessMessage() {
        when(speciesSyncService.syncAllTarantulas()).thenReturn(42);

        ResponseEntity<String> response = adminController.syncSpecies();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Sync complete. 42 species saved.");
        verify(speciesSyncService).syncAllTarantulas();
    }

    @Test
    void syncSpecies_shouldReturnZeroMessageWhenNoSpecies() {
        when(speciesSyncService.syncAllTarantulas()).thenReturn(0);

        ResponseEntity<String> response = adminController.syncSpecies();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Sync complete. 0 species saved.");
    }

    @Test
    void syncSpecies_shouldThrowWhenFamilyKeyNotFound() {
        when(speciesSyncService.syncAllTarantulas())
                .thenThrow(new IllegalStateException("Could not resolve GBIF family key for: Theraphosidae"));

        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> adminController.syncSpecies()
        );
    }

    @Test
    void syncPhotos_shouldReturnSuccessMessage() {
        ResponseEntity<String> response = adminController.syncPhotos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Photo sync complete.");
        verify(photoSyncService).syncPhotos();
    }

    @Test
    void syncAllPhotos_shouldReturnSuccessMessage() {
        ResponseEntity<String> response = adminController.syncAllPhotos();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Full photo sync complete.");
        verify(photoSyncService).syncAllPhotos();
    }
}
