package com.eventhub.user;

import com.eventhub.user.dto.UserDto;
import com.eventhub.user.dto.UserRegisterRequest;

public class UserMapper
{
    public static User toUserEntity(UserRegisterRequest request, String encodedPassword)
    {
        return User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encodedPassword)
                .role(request.getRole())
                .build();
    }

    public static UserDto toUserDto(User user)
    {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
