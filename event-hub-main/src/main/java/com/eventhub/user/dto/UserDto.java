package com.eventhub.user.dto;

import com.eventhub.user.UserRole;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class UserDto
{
    private UUID id;
    private String username;
    private String email;
    private UserRole role;
}
