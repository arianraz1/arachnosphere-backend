package com.tarantulacommunity.tarantulasocial.features.admin.controller;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.SpeciesSyncService;
import com.tarantulacommunity.tarantulasocial.features.admin.inat.PhotoSyncService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static com.tarantulacommunity.tarantulasocial.TestUtilities.apiBasePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpeciesSyncService speciesSyncService;

    @MockitoBean
    private PhotoSyncService photoSyncService;

    private final String uriPath = apiBasePath() + "/admin";

    // ==========================================
    // ENDPOINT 1: /species/sync
    // ==========================================
    @Nested
    class SpeciesSyncTests {
        @Test
        void syncSpecies_shouldReturnOk_forAdmin() throws Exception {
            when(speciesSyncService.syncAllTarantulas()).thenReturn(42);

            mockMvc.perform(post(uriPath + "/species/sync")
                            .with(csrf())
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Sync complete. 42 species saved."))
                    .andExpect(header().exists("X-Correlation-ID")); // Verifies your Logging Filter works here too!
        }

        @Test
        void syncSpecies_shouldReturnForbidden_forNonAdmin() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync")
                            .with(csrf())
                            .with(user("user").roles("USER")))
                    .andExpect(status().isForbidden());
        }

        @Test
        void syncSpecies_shouldReturnUnauthorized_whenNoUser() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync")
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================
    // ENDPOINT 2: /species/sync/photos
    // ==========================================
    @Nested
    class PhotoSyncTests {
        @Test
        void syncPhotos_shouldReturnOk_forAdmin() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync/photos")
                            .with(csrf())
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Photo sync complete."));

            // Verifies the underlying service method was actually called exactly once
            verify(photoSyncService).syncPhotos();
        }

        @Test
        void syncPhotos_shouldReturnForbidden_forNonAdmin() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync/photos")
                            .with(csrf())
                            .with(user("user").roles("USER")))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================
    // ENDPOINT 3: /species/sync/photos/full
    // ==========================================
    @Nested
    class FullPhotoSyncTests {
        @Test
        void syncAllPhotos_shouldReturnOk_forAdmin() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync/photos/full")
                            .with(csrf())
                            .with(user("admin").roles("ADMIN")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Full photo sync complete."));

            verify(photoSyncService).syncAllPhotos();
        }

        @Test
        void syncAllPhotos_shouldReturnForbidden_forNonAdmin() throws Exception {
            mockMvc.perform(post(uriPath + "/species/sync/photos/full")
                            .with(csrf())
                            .with(user("user").roles("USER")))
                    .andExpect(status().isForbidden());
        }
    }
}
