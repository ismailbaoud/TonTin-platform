package com.tontin.platform.dto.auth.login.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an access token.
 *
 * <p>This immutable record encapsulates the refresh token required to
 * obtain a new access token without requiring the user to log in again.</p>
 *
 * @param token The refresh token previously issued during login
 */
@Schema(description = "Request object for refreshing an access token")
public record RefreshTokenRequest(
    @NotBlank(message = "Refresh token is required")
    @Schema(
        description = "Refresh token issued during login",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String token
) {
    /**
     * Compact constructor for validation.
     */
    public RefreshTokenRequest {
        if (token != null) {
            token = token.trim();
        }
    }

    /**
     * Checks if the token appears to be in JWT format.
     *
     * @return true if the token has the basic JWT structure (three parts separated by dots)
     */
    public boolean isValidFormat() {
        return token != null && token.split("\\.").length == 3;
    }
}
