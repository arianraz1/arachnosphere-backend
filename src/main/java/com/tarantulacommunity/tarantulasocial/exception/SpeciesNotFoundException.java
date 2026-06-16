package com.tarantulacommunity.tarantulasocial.exception;

public class SpeciesNotFoundException extends RuntimeException {
    public SpeciesNotFoundException(Long id) {
        super("Species not found with id: " + id);
    }
}
