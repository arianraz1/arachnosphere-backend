package com.tarantulacommunity.tarantulasocial;

import com.tarantulacommunity.tarantulasocial.domain.entity.*;

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

    public static MessageEntity validMessage(UserEntity sender, UserEntity recipient, String content) {
        MessageEntity message = new MessageEntity();
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setContent(content);
        return message;
    }
}
