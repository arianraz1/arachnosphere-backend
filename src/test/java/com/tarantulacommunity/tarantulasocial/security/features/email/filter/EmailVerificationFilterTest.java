package com.tarantulacommunity.tarantulasocial.security.features.email.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static com.tarantulacommunity.tarantulasocial.TestUtilities.apiBasePath;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailVerificationFilterTest {

    private EmailVerificationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    private final String base = apiBasePath();

    @Mock
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new EmailVerificationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(boolean verified) {
        var auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.setAttribute("emailVerified", verified);
    }

    @Test
    void shouldAllowGetRequestsRegardlessOfVerification() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(base + "/collection/species/3");
        authenticateAs(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isNotEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldAllowMutatingRequestWhenVerified() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/collection");
        authenticateAs(true);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldBlockMutatingRequestWhenNotVerified() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/collection");
        authenticateAs(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldBlockPutRequestWhenNotVerified() throws Exception {
        request.setMethod("PUT");
        request.setRequestURI(base + "/collection/5");
        authenticateAs(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldBlockDeleteRequestWhenNotVerified() throws Exception {
        request.setMethod("DELETE");
        request.setRequestURI(base + "/messages/7");
        authenticateAs(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void shouldAllowAuthEndpointsEvenWhenNotVerified() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/auth/resend-verification");
        authenticateAs(false);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldAllowUnauthenticatedRequestsThrough() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/collection");
        // no authentication set in SecurityContextHolder

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void shouldBlockWhenEmailVerifiedAttributeMissing() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(base + "/collection");

        var auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        // deliberately not setting request attribute

        filter.doFilterInternal(request, response, chain);

        verify(chain, never()).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
    }
}
