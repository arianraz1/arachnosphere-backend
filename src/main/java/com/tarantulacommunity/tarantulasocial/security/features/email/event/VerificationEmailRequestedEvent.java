package com.tarantulacommunity.tarantulasocial.security.features.email.event;

public record VerificationEmailRequestedEvent(Long userId, String email, String username, String verificationLink) {
}
