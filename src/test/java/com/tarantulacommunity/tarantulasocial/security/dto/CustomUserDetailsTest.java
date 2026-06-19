package com.tarantulacommunity.tarantulasocial.security.dto;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    @Test
    void shouldReturnUserFields() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        CustomUserDetails details = new CustomUserDetails(user);

        assertThat(details.getId()).isEqualTo(1L);
        assertThat(details.getUsername()).isEqualTo("test@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).isEmpty();
        assertThat(details.isEnabled()).isTrue();
    }
}
