package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Resolves the "real" client IP for rate-limit keying.
 *
 * IMPORTANT: X-Forwarded-For is trivially spoofable by anyone who can talk
 * to your app directly. Only trust it once you put a reverse proxy (nginx,
 * an AWS load balancer, etc.) in front of the app that OVERWRITES the header
 * rather than appending to it. If you deploy straight to a single Lightsail
 * instance with no proxy in front, remove the header checks below and use
 * request.getRemoteAddr() only — otherwise an attacker can set their own
 * X-Forwarded-For and get a fresh rate-limit bucket on every request.
 */
public final class ClientIpResolver {

    private ClientIpResolver() {
    }

    public static String resolve(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            // First entry is the original client when the proxy appends correctly.
            return forwardedFor.split(",")[0].trim();
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        return request.getRemoteAddr();
    }
}