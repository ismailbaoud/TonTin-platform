package com.tontin.platform.controller;

import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserRole;
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
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Admin list users",
        description = "Returns paginated users for admin dashboard"
    )
    public ResponseEntity<AdminUserPageResponse> listUsersForAdmin(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Page<User> userPage;
        var pageable = PageRequest.of(page, size);

        if (query != null && !query.isBlank() && status != null) {
            userPage = userRepository.searchByUserNameAndStatus(
                query.trim(),
                status,
                pageable
            );
        } else if (query != null && !query.isBlank()) {
            userPage = userRepository.searchByUserName(query.trim(), pageable);
        } else if (status != null) {
            userPage = userRepository.findAllByStatus(status, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        List<AdminUserResponse> users = userPage
            .getContent()
            .stream()
            .map(user ->
                new AdminUserResponse(
                    user.getId().toString(),
                    user.getUserName(),
                    user.getEmail(),
                    user.getRole() != null ? user.getRole().name() : null,
                    user.getStatus() != null ? user.getStatus().name() : null,
                    user.getPicture() != null
                        ? Base64.getEncoder().encodeToString(user.getPicture())
                        : null
                )
            )
            .toList();

        return ResponseEntity.ok(
            new AdminUserPageResponse(
                users,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber(),
                userPage.getSize(),
                userPage.isFirst(),
                userPage.isLast()
            )
        );
    }

    @PutMapping(value = "/admin/{userId}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user status",
        description = "Updates account status for a specific user"
    )
    public ResponseEntity<AdminUserResponse> updateUserStatus(
        @PathVariable UUID userId,
        @RequestParam UserStatus status
    ) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );

        user.setStatus(status);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapToAdminResponse(saved));
    }

    @PutMapping(value = "/admin/{userId}/role", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update user role",
        description = "Updates role for a specific user"
    )
    public ResponseEntity<AdminUserResponse> updateUserRole(
        @PathVariable UUID userId,
        @RequestParam UserRole role
    ) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );

        user.setRole(role);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapToAdminResponse(saved));
    }

    @DeleteMapping(value = "/admin/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Delete user (soft delete)",
        description = "Marks a user account as DELETED"
    )
    public ResponseEntity<AdminUserResponse> deleteUser(
        @PathVariable UUID userId
    ) {
        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
            );

        user.setStatus(UserStatus.DELETED);
        User saved = userRepository.save(user);
        return ResponseEntity.ok(mapToAdminResponse(saved));
    }

    private AdminUserResponse mapToAdminResponse(User user) {
        return new AdminUserResponse(
            user.getId().toString(),
            user.getUserName(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getStatus() != null ? user.getStatus().name() : null,
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

    public record AdminUserResponse(
        String id,
        String userName,
        String email,
        String role,
        String status,
        String avatar
    ) {}

    public record AdminUserPageResponse(
        List<AdminUserResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        boolean first,
        boolean last
    ) {}
}
