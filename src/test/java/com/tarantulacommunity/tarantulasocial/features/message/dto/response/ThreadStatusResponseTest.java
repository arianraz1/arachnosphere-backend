package com.tarantulacommunity.tarantulasocial.features.message.dto.response;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadStatusResponseTest {

    @Test
    void constructor_setsActiveField() {
        ThreadStatusResponse response = new ThreadStatusResponse(true);

        assertThat(response.isActive()).isTrue();
    }

    @Test
    void constructor_setsActiveFieldFalse() {
        ThreadStatusResponse response = new ThreadStatusResponse(false);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    void noArgsConstructor_defaultsToFalse() {
        ThreadStatusResponse response = new ThreadStatusResponse();

        assertThat(response.isActive()).isFalse();
    }
}
