package com.tontin.platform.controller;

import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.service.DartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Dart (Tontine/Savings Circle) management operations.
 *
 * <p>
 * This controller handles CRUD operations for darts and requires
 * authentication with CLIENT or ADMIN role.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/dart")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Dart Management",
    description = "Endpoints for managing darts (tontines/savings circles)"
)
@SecurityRequirement(name = "Bearer Authentication")
public class DartController {

    private final DartService dartService;

    /**
     * Get all darts for the authenticated user.
     *
     * @param status optional status filter (PENDING, ACTIVE, FINISHED)
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of user's darts
     */
    @GetMapping(value = "/my-dars", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get my darts",
        description = "Retrieves all darts where the authenticated user is a member, with optional status filtering and pagination"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Darts retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PageResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions"
            ),
        }
    )
    public ResponseEntity<PageResponse<DartResponse>> getMyDarts(
        @Parameter(
            description = "Filter by dart status (optional)",
            example = "ACTIVE"
        ) @RequestParam(required = false) DartStatus status,
        @Parameter(
            description = "Page number (0-based)",
            example = "0"
        ) @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "10") @RequestParam(
            defaultValue = "10"
        ) int size
    ) {
        log.info(
            "Fetching darts for current user - status: {}, page: {}, size: {}",
            status,
            page,
            size
        );
        PageResponse<DartResponse> response = dartService.getMyDarts(
            status,
            page,
            size
        );
        log.info(
            "Retrieved {} darts for current user",
            response.getContent().size()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a dart by its ID.
     *
     * @param id the unique identifier of the dart
     * @return the dart details
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get dart by ID",
        description = "Retrieves detailed information about a specific dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Dart retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DartResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<DartResponse> getDart(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("id") UUID id
    ) {
        log.info("Fetching dart with ID: {}", id);
        DartResponse response = dartService.getDartDetails(id);
        log.info("Dart retrieved successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Creates a new dart.
     *
     * @param request the dart creation request
     * @return the created dart details
     */
    @PostMapping(
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Create a new dart",
        description = "Creates a new dart (tontine/savings circle) with the authenticated user as organizer"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Dart created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DartResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request data or validation error"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions"
            ),
        }
    )
    public ResponseEntity<DartResponse> createDart(
        @Valid @RequestBody DartRequest request
    ) {
        log.info("Creating new dart with name: {}", request.name());
        DartResponse response = dartService.createDart(request);
        log.info("Dart created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing dart.
     *
     * @param id      the unique identifier of the dart to update
     * @param request the dart update request
     * @return the updated dart details
     */
    @PutMapping(
        value = "/{id}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Update dart",
        description = "Updates an existing dart's information. Only organizers can update a dart."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Dart updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DartResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request data or validation error"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - only organizers can update darts"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<DartResponse> updateDart(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable UUID id,
        @Valid @RequestBody DartRequest request
    ) {
        log.info("Updating dart with ID: {}", id);
        DartResponse response = dartService.updateDart(request, id);
        log.info("Dart updated successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a dart.
     *
     * @param id the unique identifier of the dart to delete
     * @return the deleted dart details (for confirmation)
     */
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Delete dart",
        description = "Deletes a dart and all associated data. Only organizers can delete darts."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Dart deleted successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DartResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - only organizers can delete darts"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - cannot delete dart with active members"
            ),
        }
    )
    public ResponseEntity<DartResponse> deleteDart(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable UUID id
    ) {
        log.info("Deleting dart with ID: {}", id);
        DartResponse response = dartService.deleteDart(id);
        log.info("Dart deleted successfully: {}", id);
        return ResponseEntity.ok(response);
    }

    /**
     * Starts a dart (organizer only).
     *
     * @param id the unique identifier of the dart to start
     * @return the updated dart details
     */
    @PostMapping(
        value = "/{id}/start",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Start a dart",
        description = "Starts a dart by setting the start date and changing status to ACTIVE. Only organizers can start darts."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Dart started successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DartResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Bad request - minimum members not met"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - only organizers can start darts"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - dart already started"
            ),
        }
    )
    public ResponseEntity<DartResponse> startDart(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable UUID id
    ) {
        log.info("Starting dart with ID: {}", id);
        DartResponse response = dartService.startDart(id);
        log.info("Dart started successfully: {}", id);
        return ResponseEntity.ok(response);
    }
}
