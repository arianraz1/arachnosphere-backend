package com.tarantulacommunity.tarantulasocial.features.user.event;

public record VerificationEmailRequestedEvent(Long userId, String email, String username, String verificationLink) {
}
