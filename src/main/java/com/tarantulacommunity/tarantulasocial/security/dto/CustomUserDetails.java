package com.tarantulacommunity.tarantulasocial.security.dto;

import com.tarantulacommunity.tarantulasocial.features.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final UserEntity user;
    private final Long adminUserId;

    public Long getId() {
        return user.getId();
    }

    public UserEntity getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        boolean isAdmin = user.getId() != null
                && adminUserId != null
                && user.getId().equals(adminUserId);

        String role = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getUsername()              { return user.getEmail(); }
    @Override public String getPassword()              { return user.getPassword(); }
    @Override public boolean isAccountNonExpired()     { return true; }
    @Override public boolean isAccountNonLocked()      { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()               { return true; }
}