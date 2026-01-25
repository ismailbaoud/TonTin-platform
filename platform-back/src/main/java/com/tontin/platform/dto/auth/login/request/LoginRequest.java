package com.tontin.platform.dto.auth.login.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user authentication/login.
 *
 * <p>This immutable record encapsulates the credentials required for
 * user authentication.</p>
 *
 * @param email    User's email address (used as username)
 * @param password User's password
 */
@Schema(description = "Request object for user login")
public record LoginRequest(
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(
        description = "User's email address",
        example = "ismailbaoud46@gmail.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email,

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Schema(
        description = "User's password",
        example = "Password123@",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 8
    )
    String password
) {
    /**
     * Compact constructor for additional validation and normalization.
     */
    public LoginRequest {
        // Trim and normalize email to lowercase
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
