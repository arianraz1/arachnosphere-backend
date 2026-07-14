package com.tarantulacommunity.tarantulasocial.security.features.rate_limit.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ClientIpResolver utility class.
 * Verifies that the resolver correctly identifies client IP from various header sources.
 */
class ClientIpResolverTest {

    @Test
    void shouldResolveIpFromXForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1, 10.0.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldResolveIpFromXForwardedForHeaderWithSingleIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.5");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("203.0.113.5");
    }

    @Test
    void shouldTrimWhitespaceFromXForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", " 192.168.1.1 , 10.0.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldFallbackToXRealIpHeaderWhenXForwardedForNotPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "172.16.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("172.16.0.1");
    }

    @Test
    void shouldTrimWhitespaceFromXRealIpHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Real-IP", "  172.16.0.1  ");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("172.16.0.1");
    }

    @Test
    void shouldFallbackToRemoteAddrWhenNoProxyHeadersPresent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("127.0.0.1");
    }

    @Test
    void shouldPreferXForwardedForOverXRealIpAndRemoteAddr() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "192.168.1.1");
        request.addHeader("X-Real-IP", "172.16.0.1");
        request.setRemoteAddr("127.0.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("192.168.1.1");
    }

    @Test
    void shouldPreferXRealIpOverRemoteAddrWhenXForwardedForEmpty() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "");
        request.addHeader("X-Real-IP", "172.16.0.1");
        request.setRemoteAddr("127.0.0.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("172.16.0.1");
    }

    @Test
    void shouldUseRemoteAddrWhenAllHeadersBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "  ");
        request.addHeader("X-Real-IP", "");
        request.setRemoteAddr("192.0.2.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("192.0.2.1");
    }

    @Test
    void shouldHandleNullHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("198.51.100.1");

        String ip = ClientIpResolver.resolve(request);

        assertThat(ip).isEqualTo("198.51.100.1");
    }
}

