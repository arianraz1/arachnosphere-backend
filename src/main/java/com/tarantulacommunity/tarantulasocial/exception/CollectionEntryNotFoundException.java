package com.tarantulacommunity.tarantulasocial.exception;

public class CollectionEntryNotFoundException extends RuntimeException {
    public CollectionEntryNotFoundException(Long id) {
        super("Collection entry not found with id: " + id);
    }
}
