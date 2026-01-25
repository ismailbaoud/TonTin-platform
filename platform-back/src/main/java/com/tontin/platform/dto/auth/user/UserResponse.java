package com.tontin.platform.dto.auth.user;

import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for User information.
 *
 * <p>This immutable record represents user account information returned
 * to clients, excluding sensitive data like passwords.</p>
 *
 * @param id        Unique identifier of the user
 * @param userName  Username of the user
 * @param email     Email address of the user
 * @param role      User's role (ADMIN or CLIENT)
 * @param status    Current account status
 * @param createdAt Timestamp when the account was created
 * @param updatedAt Timestamp when the account was last updated
 */
@Builder
@Schema(description = "Response object containing user information")
public record UserResponse(
    @Schema(
        description = "Unique identifier of the user",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID id,

    @Schema(description = "Username of the user", example = "john_doe")
    String userName,

    @Schema(
        description = "Email address of the user",
        example = "john.doe@example.com"
    )
    String email,

    @Schema(
        description = "Account creation date represented as a string",
        example = "2024-01-10"
    )
    String creationDate,

    @Schema(
        description = "Indicates whether the user's email has been confirmed",
        example = "true"
    )
    Boolean emailConfirmed,

    @Schema(
        description = "Number of files the user is allowed to access",
        example = "5"
    )
    Integer accountAccessFileCount,

    @Schema(
        description = "Timestamp of the last password reset request",
        example = "2024-01-20T09:45:00"
    )
    LocalDateTime resetPasswordDate,

    @Schema(
        description = "User's role in the system",
        example = "ROLE_CLIENT",
        allowableValues = { "ROLE_ADMIN", "ROLE_CLIENT" }
    )
    UserRole role,

    @Schema(description = "User profile picture stored as binary data")
    byte[] picture,

    @Schema(
        description = "Current status of the user account",
        example = "ACTIVE",
        allowableValues = {
            "PENDING", "ACTIVE", "SUSPENDED", "DISABLED", "DELETED",
        }
    )
    UserStatus status,

    @Schema(
        description = "Timestamp when the user account was created",
        example = "2024-01-01T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the user account was last updated",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime updatedAt
) {
    /**
     * Checks if the user account is active.
     *
     * @return true if the user status is ACTIVE
     */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Checks if the user account is pending verification.
     *
     * @return true if the user status is PENDING
     */
    public boolean isPending() {
        return status == UserStatus.PENDING;
    }

    /**
     * Checks if the user account is suspended.
     *
     * @return true if the user status is SUSPENDED
     */
    public boolean isSuspended() {
        return status == UserStatus.SUSPENDED;
    }

    /**
     * Checks if the user is an administrator.
     *
     * @return true if the user role is ROLE_ADMIN
     */
    public boolean isAdmin() {
        return role == UserRole.ROLE_ADMIN;
    }

    /**
     * Checks if the user is a regular client.
     *
     * @return true if the user role is ROLE_CLIENT
     */
    public boolean isClient() {
        return role == UserRole.ROLE_CLIENT;
    }

    /**
     * Checks if the user's email has been confirmed.
     *
     * @return true if the email is confirmed
     */
    public boolean isEmailConfirmed() {
        return Boolean.TRUE.equals(emailConfirmed);
    }

    /**
     * Gets a display-friendly name for the user.
     *
     * @return the username or email if username is not available
     */
    public String getDisplayName() {
        return userName != null && !userName.isEmpty() ? userName : email;
    }
}
