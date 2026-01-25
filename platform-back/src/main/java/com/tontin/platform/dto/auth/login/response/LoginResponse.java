package com.tontin.platform.dto.auth.login.response;

import com.tontin.platform.dto.auth.user.UserResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

/**
 * Response DTO for successful user login.
 *
 * <p>This immutable record contains all information returned after a successful
 * authentication, including JWT tokens and user details.</p>
 *
 * @param token        JWT access token for API authentication
 * @param refreshToken JWT refresh token for obtaining new access tokens
 * @param user         Complete user information
 */
@Builder
@Schema(
    description = "Response object containing authentication tokens and user information after successful login"
)
public record LoginResponse(
    @Schema(
        description = "JWT access token for API authentication (expires in 15 minutes)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwiaWF0IjoxNzA2MTgwNDAwLCJleHAiOjE3MDYxODEzMDB9.signature",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String token,

    @Schema(
        description = "JWT refresh token for obtaining new access tokens (expires in 7 days)",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwidHlwZSI6InJlZnJlc2giLCJpYXQiOjE3MDYxODA0MDAsImV4cCI6MTcwNjc4NTIwMH0.signature",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String refreshToken,

    @Schema(
        description = "User information including username, email, role, and status",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UserResponse user
) {
    /**
     * Checks if all required fields are present.
     *
     * @return true if token, refreshToken, and user are all non-null
     */
    public boolean isComplete() {
        return token != null && refreshToken != null && user != null;
    }

    /**
     * Checks if the access token appears to be in JWT format.
     *
     * @return true if the token has the basic JWT structure
     */
    public boolean hasValidTokenFormat() {
        return token != null && token.split("\\.").length == 3;
    }

    /**
     * Checks if the refresh token appears to be in JWT format.
     *
     * @return true if the refresh token has the basic JWT structure
     */
    public boolean hasValidRefreshTokenFormat() {
        return refreshToken != null && refreshToken.split("\\.").length == 3;
    }

    /**
     * Gets the username from the user object.
     *
     * @return username or null if user is null
     */
    public String getUserName() {
        return user != null ? user.userName() : null;
    }

    /**
     * Gets the email from the user object.
     *
     * @return email or null if user is null
     */
    public String getEmail() {
        return user != null ? user.email() : null;
    }
}
