package com.tontin.platform.dto.auth.login.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Response DTO for token refresh operations.
 *
 * <p>This immutable record contains the new access token and the refresh token
 * after a successful token refresh request.</p>
 *
 * @param accessToken  The new JWT access token for API authentication
 * @param refreshToken The refresh token (same as provided in request)
 */
@Schema(
    description = "Response object containing authentication tokens after refresh"
)
public record AuthenticationResponse(
    @NotBlank
    @Schema(
        description = "New JWT access token for API authentication",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA2MTgwNDAwLCJleHAiOjE3MDYxODEzMDB9.signature",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String accessToken,

    @NotBlank
    @Schema(
        description = "Refresh token for obtaining new access tokens",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MDYxODA0MDAsImV4cCI6MTcwNjc4NTIwMH0.signature",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String refreshToken
) {
    /**
     * Checks if both tokens are present.
     *
     * @return true if both access token and refresh token are not null and not empty
     */
    public boolean isValid() {
        return (
            accessToken != null &&
            !accessToken.isEmpty() &&
            refreshToken != null &&
            !refreshToken.isEmpty()
        );
    }

    /**
     * Checks if the access token appears to be in JWT format.
     *
     * @return true if the access token has the basic JWT structure
     */
    public boolean hasValidAccessTokenFormat() {
        return accessToken != null && accessToken.split("\\.").length == 3;
    }

    /**
     * Checks if the refresh token appears to be in JWT format.
     *
     * @return true if the refresh token has the basic JWT structure
     */
    public boolean hasValidRefreshTokenFormat() {
        return refreshToken != null && refreshToken.split("\\.").length == 3;
    }
}
