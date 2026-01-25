package com.tontin.platform.controller;

import com.tontin.platform.config.JwtService;
import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.login.request.RefreshTokenRequest;
import com.tontin.platform.dto.auth.login.response.AuthenticationResponse;
import com.tontin.platform.dto.auth.login.response.LoginResponse;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.dto.auth.user.UserProfileUpdateRequest;
import com.tontin.platform.dto.auth.user.UserResponse;
import com.tontin.platform.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.io.UnsupportedEncodingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for authentication and user management operations.
 *
 * <p>This controller handles user registration, login, email verification,
 * token refresh, and logout operations.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Authentication",
    description = "Endpoints for user authentication and account management"
)
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final SecurityUtils securityUtils;

    /**
     * Authenticates a user and returns JWT tokens.
     *
     * @param request the login credentials
     * @return response containing access token, refresh token, and user information
     */
    @PostMapping(
        value = "/login",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password, returns JWT tokens and user information"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = LoginResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid credentials or validation error"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Authentication failed - invalid email or password"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Account is not active (pending verification, suspended, or disabled)"
            ),
        }
    )
    public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login attempt for email: {}", request.email());
        LoginResponse response = authService.login(request);
        log.info("Login successful for email: {}", request.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration details
     * @param httpRequest the HTTP servlet request (used to generate verification URL)
     * @return response containing the newly created user information
     * @throws UnsupportedEncodingException if email encoding fails
     * @throws MessagingException if email sending fails
     */
    @PostMapping(
        value = "/register",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Register new user",
        description = "Creates a new user account and sends a verification email"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "User registered successfully, verification email sent",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid input or validation error"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Email already exists"
            ),
            @ApiResponse(
                responseCode = "500",
                description = "Failed to send verification email"
            ),
        }
    )
    public ResponseEntity<UserResponse> register(
        @Valid @RequestBody RegisterRequest request,
        HttpServletRequest httpRequest
    ) throws UnsupportedEncodingException, MessagingException {
        log.info("Registration attempt for email: {}", request.email());
        String siteUrl = getSiteUrl(httpRequest);
        UserResponse response = authService.register(request, siteUrl);
        log.info(
            "User registered successfully with email: {}. Verification email sent.",
            request.email()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Verifies a user's email address using the verification code.
     *
     * @param code the verification code sent to the user's email
     * @return success message if verification is successful
     */
    @GetMapping(value = "/verify", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Verify email",
        description = "Verifies a user's email address using the code sent via email"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Email verified successfully"
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or expired verification code"
            ),
            @ApiResponse(responseCode = "404", description = "User not found"),
        }
    )
    public ResponseEntity<MessageResponse> verify(
        @RequestParam("code") @NotBlank(
            message = "Verification code is required"
        ) @Parameter(
            description = "Verification code sent to user's email",
            required = true,
            example = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6q7r8s9t0u1v2w3x4y5z6"
        ) String code
    ) {
        log.info("Email verification attempt with code: {}", code);
        String message = authService.verify(code);
        log.info("Email verification successful for code: {}", code);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    /**
     * Logs out the current user.
     *
     * @return success message
     */
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "User logout",
        description = "Logs out the current user (client should discard tokens)"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Logout successful"
            ),
        }
    )
    public ResponseEntity<MessageResponse> logout() {
        // Get current user details using SecurityUtils
        String userEmail = securityUtils
            .getCurrentUserEmail()
            .orElse("unknown");
        log.info("Logout request received from user: {}", userEmail);

        authService.logout();
        securityUtils.clearSecurityContext();

        return ResponseEntity.ok(
            new MessageResponse(
                "Logout successful. Please discard your tokens."
            )
        );
    }

    /**
     * Gets the currently authenticated user's profile information.
     *
     * @return the current user's details
     */
    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Get current user",
        description = "Retrieves the profile information of the currently authenticated user"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "User profile retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
        }
    )
    public ResponseEntity<UserResponse> getCurrentUser() {
        log.info("Fetching current user profile");

        UserResponse response = authService.getCurrentUserProfile();

        log.info("Current user profile retrieved: {}", response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates the currently authenticated user's profile.
     *
     * @param request profile update payload
     * @return updated user details
     */
    @PutMapping(
        value = "/me",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Update current user profile",
        description = "Allows the authenticated user to update their profile information such as display name, password, and profile picture"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "User profile updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UserResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid profile data supplied"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
        }
    )
    public ResponseEntity<UserResponse> updateCurrentUser(
        @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        log.info("Updating current user profile");
        UserResponse response = authService.updateCurrentUserProfile(request);
        log.info("Current user profile updated: {}", response.email());
        return ResponseEntity.ok(response);
    }

    /**
     * Refreshes an access token using a valid refresh token.
     *
     * @param request the refresh token request
     * @return response containing new access token and the same refresh token
     */
    @PostMapping(
        value = "/refresh-token",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Refresh access token",
        description = "Generates a new access token using a valid refresh token"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Token refreshed successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation = AuthenticationResponse.class
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid or malformed refresh token"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Refresh token expired or invalid"
            ),
        }
    )
    public ResponseEntity<AuthenticationResponse> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.info("Token refresh request received");

        String username = jwtService.extractUsername(request.token());
        UserDetails userDetails = userDetailsService.loadUserByUsername(
            username
        );

        if (!jwtService.isRefreshTokenValid(request.token(), userDetails)) {
            log.warn("Invalid refresh token for user: {}", username);
            throw new IllegalArgumentException(
                "Invalid or expired refresh token"
            );
        }

        String newAccessToken = jwtService.generateToken(userDetails);
        log.info("Token refreshed successfully for user: {}", username);

        return ResponseEntity.ok(
            new AuthenticationResponse(newAccessToken, request.token())
        );
    }

    /**
     * Extracts the base site URL from the HTTP request.
     *
     * @param request the HTTP servlet request
     * @return the base site URL
     */
    private String getSiteUrl(HttpServletRequest request) {
        String siteUrl = request.getRequestURL().toString();
        return siteUrl.replace(request.getServletPath(), "");
    }

    /**
     * Simple record for message responses.
     *
     * @param message the message to return to the client
     */
    @Schema(description = "Simple message response")
    public record MessageResponse(
        @Schema(
            description = "Response message",
            example = "Operation completed successfully"
        ) String message
    ) {}
}
