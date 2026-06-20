package com.tarantulacommunity.tarantulasocial.features.admin.controller;

import com.tarantulacommunity.tarantulasocial.features.admin.gbif.SpeciesSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SpeciesSyncService speciesSyncService;

    @Test
    void syncSpecies_shouldReturnOk_forAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/species/sync")
                        .with(csrf())
                        .with(user("admin").roles("ADMIN")))  // ✅ Explicitly set user with role
                .andExpect(status().isOk());
    }

    @Test
    void syncSpecies_shouldReturnForbidden_forNonAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/species/sync")
                        .with(csrf())
                        .with(user("user").roles("USER")))   // ✅ Non-admin role
                .andExpect(status().isForbidden());
    }

    @Test
    void syncSpecies_shouldReturnUnauthorized_whenNoUser() throws Exception {
        mockMvc.perform(post("/api/admin/species/sync")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}