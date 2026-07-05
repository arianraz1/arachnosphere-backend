package com.tarantulacommunity.tarantulasocial.security.event;

public record VerificationEmailRequestedEvent(Long userId, String email, String username, String verificationLink) {
}
