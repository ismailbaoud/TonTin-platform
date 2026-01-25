package com.tontin.platform.dto.auth.register.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 *
 * <p>This immutable record encapsulates all required information for
 * creating a new user account.</p>
 *
 * @param userName Username for the new account
 * @param email    User's email address (must be unique)
 * @param password User's password (must meet security requirements)
 */
@Schema(description = "Request object for user registration")
public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(
        min = 3,
        max = 50,
        message = "Username must be between 3 and 50 characters"
    )
    @Pattern(
        regexp = "^[a-zA-Z0-9_-]+$",
        message = "Username can only contain letters, numbers, underscores, and hyphens"
    )
    @Schema(
        description = "Username for the new account",
        example = "john_doe",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 50
    )
    String userName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    @Schema(
        description = "User's email address (must be unique)",
        example = "ismailbaoud46@gmail.com",
        requiredMode = Schema.RequiredMode.REQUIRED,
        maxLength = 100
    )
    String email,

    @NotBlank(message = "Password is required")
    @Size(
        min = 8,
        max = 100,
        message = "Password must be between 8 and 100 characters"
    )
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    @Schema(
        description = "User's password (must meet security requirements: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char)",
        example = "Password123@",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8,
        maxLength = 100,
        format = "password"
    )
    String password
) {
    /**
     * Compact constructor for additional validation and normalization.
     */
    public RegisterRequest {
        // Trim and normalize inputs
        if (userName != null) {
            userName = userName.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }

    /**
     * Checks if the password meets minimum security requirements.
     *
     * @return true if password is strong enough
     */
    public boolean hasStrongPassword() {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.matches(".*[@$!%*?&].*");

        return hasUpper && hasLower && hasDigit && hasSpecial;
    }
}
