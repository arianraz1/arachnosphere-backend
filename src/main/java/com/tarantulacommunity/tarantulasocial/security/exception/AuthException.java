package com.tarantulacommunity.tarantulasocial.security.exception;

public abstract class AuthException extends RuntimeException {
    protected AuthException(String message) { super(message); }
}
