package com.tarantulacommunity.tarantulasocial.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Bean
    public OpenAPI arachnosphereOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Arachnosphere API")
                        .description("Internal REST API for the Arachnosphere platform.")
                        .version("1.0.0"))
                .servers(List.of(
                        new Server().url(baseUrl).description("Production Server"),
                        new Server().url("http://localhost:8080").description("Development Server")
                ));
    }
}
