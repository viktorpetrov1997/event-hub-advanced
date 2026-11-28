package com.eventhub.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileEditRequest
{
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    @Pattern(
            regexp = "^[a-zA-Z](?:[a-zA-Z0-9]|[._](?=[a-zA-Z0-9]))*$",
            message = "Username must start with a letter, contain only letters, numbers, dots and underscores, and cannot end with or contain consecutive dots or underscores"
    )
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
