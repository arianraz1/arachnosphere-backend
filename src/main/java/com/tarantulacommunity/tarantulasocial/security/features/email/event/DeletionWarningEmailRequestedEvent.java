package com.tarantulacommunity.tarantulasocial.security.features.email.event;

public record DeletionWarningEmailRequestedEvent(
        Long userId, String email, String username, String verificationLink) {}