package com.tarantulacommunity.tarantulasocial;

import com.tarantulacommunity.tarantulasocial.features.collection_entry.entity.CollectionEntryEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.MessageEntity;
import com.tarantulacommunity.tarantulasocial.features.message.entity.ThreadEntity;
import com.tarantulacommunity.tarantulasocial.features.species.entity.SpeciesEntity;
import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;

public class TestUtilities {

    public static UserEntity validUser(String emailSuffix, String usernameSuffix) {
        UserEntity user = new UserEntity();
        user.setEmail("test" + emailSuffix + "@example.com");
        user.setUsername("test" + usernameSuffix);
        user.setPassword("$2a$10$abcdefghijklmnopqrstuv");
        return user;
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
}
