package com.tarantulacommunity.tarantulasocial.security.config;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded");
        user.setEmailVerified(true);
    }

    // -------------------------------------------------------------------------
    // Basic field mapping
    // -------------------------------------------------------------------------

    @Test
    void shouldReturnCorrectId() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.getId()).isEqualTo(1L);
    }

    @Test
    void shouldReturnEmailAsUsername() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnEncodedPassword() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.getPassword()).isEqualTo("encoded");
    }

    @Test
    void shouldReturnUserEntity() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.getUser()).isSameAs(user);
    }

    // -------------------------------------------------------------------------
    // Account status flags
    // -------------------------------------------------------------------------

    @Test
    void shouldAlwaysBeEnabled() {
        user.setEmailVerified(true);
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldBeEnabledEvenWhenEmailNotVerified() {
        user.setEmailVerified(false);
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.isEnabled()).isTrue();
    }

    @Test
    void shouldBeAccountNonExpired() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.isAccountNonExpired()).isTrue();
    }

    @Test
    void shouldBeAccountNonLocked() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.isAccountNonLocked()).isTrue();
    }

    @Test
    void shouldBeCredentialsNonExpired() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.isCredentialsNonExpired()).isTrue();
    }

    // -------------------------------------------------------------------------
    // Role / authority assignment
    // -------------------------------------------------------------------------

    @Test
    void shouldGrantAdminRoleWhenUserIdMatchesAdminId() {
        CustomUserDetails details = new CustomUserDetails(user, 1L);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void shouldGrantUserRoleWhenUserIdDoesNotMatchAdminId() {
        CustomUserDetails details = new CustomUserDetails(user, 99L);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldGrantUserRoleForRegularUser() {
        user.setId(2L);
        CustomUserDetails details = new CustomUserDetails(user, 1L);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_USER");
    }

    @Test
    void shouldNotGrantAdminToUserWithDifferentId() {
        user.setId(2L);
        CustomUserDetails details = new CustomUserDetails(user, 1L);
        assertThat(details.getAuthorities())
                .extracting("authority")
                .doesNotContain("ROLE_ADMIN");
    }

    @Test
    void shouldReturnExactlyOneAuthority() {
        CustomUserDetails details = new CustomUserDetails(user, 1L);
        assertThat(details.getAuthorities()).hasSize(1);
    }
}