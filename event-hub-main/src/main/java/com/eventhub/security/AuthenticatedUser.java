package com.eventhub.security;

import com.eventhub.user.UserRole;

import lombok.Builder;
import lombok.Getter;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AuthenticatedUser implements UserDetails
{
    private UUID id;
    private String displayName;
    private String email;
    private String password;
    private UserRole role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername()
    {
        return email;
    }
}
