package com.tarantulacommunity.tarantulasocial.exception;

public class EmailAlreadyTakenException extends RuntimeException {
    public EmailAlreadyTakenException(String email) {
        super("Email already taken: " + email);
    }
}
