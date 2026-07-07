package com.tarantulacommunity.tarantulasocial.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OpenApiConfigTest {

    @InjectMocks
    private OpenApiConfig openApiConfig;

    @Test
    void arachnosphereOpenAPI_shouldReturnValidOpenAPI() {
        OpenAPI openAPI = openApiConfig.arachnosphereOpenAPI();

        assertThat(openAPI).isNotNull();
    }

    @Test
    void arachnosphereOpenAPI_shouldHaveCorrectInfo() {
        OpenAPI openAPI = openApiConfig.arachnosphereOpenAPI();
        Info info = openAPI.getInfo();

        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Arachnosphere API");
        assertThat(info.getDescription()).isEqualTo("Internal REST API for the Arachnosphere platform.");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void arachnosphereOpenAPI_shouldHaveServers() {
        OpenAPI openAPI = openApiConfig.arachnosphereOpenAPI();
        List<Server> servers = openAPI.getServers();

        assertThat(servers).isNotNull();
        assertThat(servers).hasSize(2);
    }

    @Test
    void arachnosphereOpenAPI_shouldHaveProductionServer() {
        OpenAPI openAPI = openApiConfig.arachnosphereOpenAPI();
        List<Server> servers = openAPI.getServers();

        Server productionServer = servers.get(0);
        assertThat(productionServer).isNotNull();
        assertThat(productionServer.getDescription()).isEqualTo("Production Server");
    }

    @Test
    void arachnosphereOpenAPI_shouldHaveDevelopmentServer() {
        OpenAPI openAPI = openApiConfig.arachnosphereOpenAPI();
        List<Server> servers = openAPI.getServers();

        Server developmentServer = servers.get(1);
        assertThat(developmentServer).isNotNull();
        assertThat(developmentServer.getUrl()).isEqualTo("http://localhost:8080");
        assertThat(developmentServer.getDescription()).isEqualTo("Development Server");
    }
}
