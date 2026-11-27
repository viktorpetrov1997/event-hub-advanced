package com.eventhub.user.dto;

import com.eventhub.user.UserRole;

import jakarta.validation.constraints.*;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRegisterRequest
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

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @AssertTrue(message = "Passwords must match")
    public boolean isPasswordMatching()
    {
        return password == null || confirmPassword == null || password.equals(confirmPassword);
    }

    @NotNull(message = "Role is required")
    private UserRole role;

    @AssertTrue(message = "Invalid role")
    public boolean isRoleAllowed()
    {
        return role != UserRole.ADMIN;
    }
}
