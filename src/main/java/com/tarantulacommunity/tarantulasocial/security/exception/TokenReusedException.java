package com.tarantulacommunity.tarantulasocial.security.exception;

public class TokenReusedException extends AuthException {
    public TokenReusedException() {
        super("Refresh token reuse detected — all sessions invalidated");
    }
}
