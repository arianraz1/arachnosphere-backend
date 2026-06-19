package com.tarantulacommunity.tarantulasocial.security.exception;

public class InvalidRefreshTokenException extends AuthException {
    public InvalidRefreshTokenException(String message) { super(message); }
}
