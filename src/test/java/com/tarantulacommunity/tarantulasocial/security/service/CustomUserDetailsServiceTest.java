package com.tarantulacommunity.tarantulasocial.security.service;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import com.tarantulacommunity.tarantulasocial.features.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private CustomUserDetailsService service;

    @Test
    void shouldLoadUserByEmail() {
        UserEntity user = new UserEntity();
        user.setEmail("u@example.com");
        given(userRepository.findByEmail("u@example.com")).willReturn(Optional.of(user));

        var details = service.loadUserByUsername("u@example.com");
        assertThat(details.getUsername()).isEqualTo("u@example.com");
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        given(userRepository.findByEmail("missing")).willReturn(Optional.empty());
        assertThatThrownBy(() -> service.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldNormalizeEmailToLowercaseBeforeLookup() {
        UserEntity user = new UserEntity();
        user.setEmail("u@example.com");
        given(userRepository.findByEmail("u@example.com")).willReturn(Optional.of(user));

        var details = service.loadUserByUsername("U@EXAMPLE.COM");
        assertThat(details.getUsername()).isEqualTo("u@example.com");
    }

    @Test
    void shouldTrimWhitespaceFromEmail() {
        UserEntity user = new UserEntity();
        user.setEmail("u@example.com");
        given(userRepository.findByEmail("u@example.com")).willReturn(Optional.of(user));

        var details = service.loadUserByUsername("  u@example.com  ");
        assertThat(details.getUsername()).isEqualTo("u@example.com");
    }
}