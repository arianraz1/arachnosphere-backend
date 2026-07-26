package com.tarantulacommunity.tarantulasocial.s3;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class S3Config {

    private String bucket;
    private String region;
    private String endpointUrl;        // R2 API endpoint (e.g. https://<account_id>.r2.cloudflarestorage.com)
    private String publicEndpointUrl;  // public-facing URL for serving images (r2.dev or custom domain)
    private String accessKeyId;
    private String secretAccessKey;

    private boolean publicUrlIncludesBucket = true;

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

        if (accessKeyId != null && !accessKeyId.isBlank()) {
            builder.credentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    )
            );
        }

        return builder.build();
    }
}