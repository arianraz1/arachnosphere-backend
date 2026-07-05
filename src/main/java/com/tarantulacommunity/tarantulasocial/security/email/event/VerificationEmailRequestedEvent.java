package com.tarantulacommunity.tarantulasocial.security.email.event;

public record VerificationEmailRequestedEvent(Long userId, String email, String username, String verificationLink) {
}
