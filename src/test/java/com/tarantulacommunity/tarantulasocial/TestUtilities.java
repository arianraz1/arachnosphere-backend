package com.tarantulacommunity.tarantulasocial;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.s3.S3Config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;

public class TestUtilities {

    private static String cachedBasePath;

    /**
     * Resolve the API base path from app.base-url property in application.properties on the classpath.
     * Falls back to "/api/v1" if not found or malformed.
     */
    public static String apiBasePath() {
        if (cachedBasePath != null) return cachedBasePath;
        String defaultPath = "/api/v1";
        try (InputStream in = TestUtilities.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (in != null) {
                Properties p = new Properties();
                p.load(in);
                String baseUrl = p.getProperty("app.base-url");
                if (baseUrl != null && !baseUrl.isBlank()) {
                    String path = URI.create(baseUrl).getPath();
                    cachedBasePath = (path == null || path.isBlank()) ? defaultPath : path;
                    return cachedBasePath;
                }
            }
        } catch (IOException ignored) {
        } catch (Exception e) {
            // malformed URI, ignore
        }
        cachedBasePath = defaultPath;
        return cachedBasePath;
    }

    public static UserEntity validUser(String emailSuffix, String usernameSuffix, boolean emailVerified) {
        UserEntity user = new UserEntity();
        user.setEmail("test" + emailSuffix + "@example.com");
        user.setUsername("test" + usernameSuffix);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        user.setEmailVerified(emailVerified);
        return user;
    }

    public static UserEntity verifiedUser(String emailSuffix, String usernameSuffix) {
        return validUser(emailSuffix, usernameSuffix, true);
    }

    public static UserEntity unverifiedUser(String emailSuffix, String usernameSuffix) {
        return validUser(emailSuffix, usernameSuffix, false);
    }

    public static SpeciesEntity validSpecies(long gbifKey, String scientificName) {
        SpeciesEntity species = new SpeciesEntity();
        species.setGbifNubKey(gbifKey);
        species.setScientificName(scientificName);
        return species;
    }

    public static CollectionEntryEntity validCollectionEntry(UserEntity user, SpeciesEntity species) {
        CollectionEntryEntity entry = new CollectionEntryEntity();
        entry.setUser(user);
        entry.setSpecies(species);
        return entry;
    }

    public static ThreadEntity validThread(UserEntity createdBy) {
        ThreadEntity thread = new ThreadEntity();
        thread.setCreatedBy(createdBy);
        thread.setSubject("Test subject");
        return thread;
    }

    public static MessageEntity validMessage(UserEntity sender, UserEntity recipient, String content, ThreadEntity thread) {
        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        message.setThread(thread);
        return message;
    }

    public static String buildExpectedImageUrl(S3Config s3Config, String key) {
        if (s3Config.isUsingCustomEndpoint()) {
            return String.format("%s/%s/%s",
                    s3Config.getEndpointUrl(),
                    s3Config.getBucket(),
                    key);
        }

        return String.format("https://%s.s3.%s.amazonaws.com/%s",
                s3Config.getBucket(),
                s3Config.getRegion(),
                key);
    }

}
