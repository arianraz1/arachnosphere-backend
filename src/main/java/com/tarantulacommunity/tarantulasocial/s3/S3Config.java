package com.tarantulacommunity.tarantulasocial.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Config {

    private String bucket;
    private String region;
    private String endpointUrl;        // internal SDK endpoint (e.g. http://localstack:4566)
    private String publicEndpointUrl;  // browser-facing endpoint (e.g. http://localhost:4566), dev/LocalStack only

    public boolean isUsingCustomEndpoint() {
        return endpointUrl != null && !endpointUrl.isBlank();
    }

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region));

        if (isUsingCustomEndpoint()) {
            builder
                    .endpointOverride(URI.create(endpointUrl))
                    .forcePathStyle(true);
        }

        return builder.build();
    }
}
