package com.tontin.platform.controller;

import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserStatus;
import com.tontin.platform.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for User management operations.
 *
 * <p>
 * This controller handles operations related to users, including
 * searching users by username. Requires authentication with CLIENT or ADMIN role.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "User Management", description = "Endpoints for managing users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserRepository userRepository;

    /**
     * Search users by username.
     *
     * @param username the username query to search for
     * @return list of matching users
     */
    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Search users by username",
        description = "Searches for users by username (case-insensitive, partial match). Returns user basic information for inviting to darts."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Users found",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                        schema = @Schema(implementation = UserSearchResponse.class)
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - invalid search query"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
        }
    )
    public ResponseEntity<List<UserSearchResponse>> searchUsers(
        @Parameter(
            description = "Username to search for (partial match)",
            required = true,
            example = "john"
        ) @RequestParam String username
    ) {
        log.info("Searching users with username: {}", username);

        if (username == null || username.trim().isEmpty()) {
            log.warn("Empty username provided for search");
            return ResponseEntity.ok(List.of());
        }

        if (username.trim().length() < 2) {
            log.warn("Username search query too short: {}", username);
            return ResponseEntity.ok(List.of());
        }

        List<User> users = userRepository.findByUserNameAndStatus(username.trim(), UserStatus.ACTIVE);

        List<UserSearchResponse> response = users
            .stream()
            .map(this::mapToSearchResponse)
            .collect(Collectors.toList());

        log.info("Found {} users matching '{}'", response.size(), username);
        return ResponseEntity.ok(response);
    }

    private UserSearchResponse mapToSearchResponse(User user) {
        return new UserSearchResponse(
            user.getId().toString(),
            user.getUserName(),
            user.getEmail(),
            user.getPicture() != null
                ? Base64.getEncoder().encodeToString(user.getPicture())
                : null
        );
    }

    /**
     * Response DTO for user search results.
     */
    @Schema(description = "User search result")
    public record UserSearchResponse(
        @Schema(description = "User ID", example = "123e4567-e89b-12d3-a456-426614174000") String id,
        @Schema(description = "Username", example = "johndoe") String userName,
        @Schema(description = "Email address", example = "john@example.com") String email,
        @Schema(description = "Base64 encoded avatar image", example = "iVBORw0KGgoAAAANS...") String avatar
    ) {}
}
