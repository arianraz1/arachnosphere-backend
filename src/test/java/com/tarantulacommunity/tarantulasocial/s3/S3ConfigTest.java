package com.tarantulacommunity.tarantulasocial.s3;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

class S3ConfigTest {

    @Test
    void bucketGetterSetter() {
        S3Config config = new S3Config();
        config.setBucket("my-bucket");
        assertEquals("my-bucket", config.getBucket());
    }

    @Test
    void regionGetterSetter() {
        S3Config config = new S3Config();
        config.setRegion("us-east-1");
        assertEquals("us-east-1", config.getRegion());
    }

    @Test
    void endpointUrlGetterSetter() {
        S3Config config = new S3Config();
        config.setEndpointUrl("http://localhost:4566");
        assertEquals("http://localhost:4566", config.getEndpointUrl());
    }

    @Test
    void s3Client_buildsWithRegionOnly_whenEndpointUrlIsNull() {
        S3Config config = new S3Config();
        config.setBucket("my-bucket");
        config.setRegion("us-east-1");
        config.setEndpointUrl(null);

        S3Client client = config.s3Client();

        assertNotNull(client);
        assertEquals(Region.US_EAST_1, client.serviceClientConfiguration().region());
        assertTrue(client.serviceClientConfiguration().endpointOverride().isEmpty());
    }

    @Test
    void s3Client_buildsWithRegionOnly_whenEndpointUrlIsBlank() {
        S3Config config = new S3Config();
        config.setBucket("my-bucket");
        config.setRegion("us-west-2");
        config.setEndpointUrl("   ");

        S3Client client = config.s3Client();

        assertNotNull(client);
        assertEquals(Region.US_WEST_2, client.serviceClientConfiguration().region());
        assertTrue(client.serviceClientConfiguration().endpointOverride().isEmpty());
    }

    @Test
    void s3Client_appliesEndpointOverride_whenEndpointUrlIsSet() {
        S3Config config = new S3Config();
        config.setBucket("my-bucket");
        config.setRegion("us-east-1");
        config.setEndpointUrl("http://localhost:4566");

        S3Client client = config.s3Client();

        assertNotNull(client);
        assertTrue(client.serviceClientConfiguration().endpointOverride().isPresent());
        assertEquals(URI.create("http://localhost:4566"),
                client.serviceClientConfiguration().endpointOverride().get());
    }

    @Test
    void s3Client_throwsWhenRegionIsNull() {
        S3Config config = new S3Config();
        config.setBucket("my-bucket");
        config.setRegion(null);

        assertThrows(NullPointerException.class, config::s3Client);
    }

    @Test
    void publicEndpointUrlGetterSetter() {
        S3Config config = new S3Config();
        config.setPublicEndpointUrl("http://localhost:4566");
        assertEquals("http://localhost:4566", config.getPublicEndpointUrl());
    }

    @Test
    void publicEndpointUrl_defaultsToNull() {
        S3Config config = new S3Config();
        assertNull(config.getPublicEndpointUrl());
    }
}