package com.tontin.platform.dto.auth.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request DTO used to update a user's profile information.
 *
 * <p>All fields are optional; only non-null values will be applied. Validation
 * is still enforced when a field is provided.</p>
 *
 * @param userName New display name for the user (optional)
 * @param password New password for the user (optional)
 * @param picture  Profile picture as a binary payload (optional)
 */
@Schema(description = "Request object for updating user profile information")
public record UserProfileUpdateRequest(
    @Schema(
        description = "Desired display name",
        example = "john_updated",
        minLength = 3,
        maxLength = 50
    )
    @Size(
        min = 3,
        max = 50,
        message = "Username must be between 3 and 50 characters"
    )
    String userName,

    @Schema(
        description = "New password for the account",
        example = "N3wStr0ngP@ssword!",
        minLength = 8
    )
    @Size(
        min = 8,
        message = "Password must contain at least 8 characters"
    )
    String password,

    @Schema(
        description = "Profile picture bytes (e.g., JPEG or PNG)",
        nullable = true
    )
    byte[] picture
) {}
