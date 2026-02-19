package com.tontin.platform.controller;

import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.round.request.CreateRoundsRequest;
import com.tontin.platform.dto.round.request.RoundRequest;
import com.tontin.platform.dto.round.response.RoundResponse;
import com.tontin.platform.service.RoundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
 * REST Controller for Round management operations.
 *
 * <p>
 * This controller handles CRUD operations for rounds and requires
 * authentication with CLIENT or ADMIN role.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/rounds")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Round Management",
    description = "Endpoints for managing rounds in darts"
)
@SecurityRequirement(name = "Bearer Authentication")
public class RoundController {

    private final RoundService roundService;

    /**
     * Create rounds for a dart when it starts.
     *
     * @param request the create rounds request
     * @return list of created rounds
     */
    @PostMapping(
        value = "/create-for-dart",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Create rounds for a dart",
        description = "Creates rounds for a dart based on its order method and member count. Only organizers can create rounds."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Rounds created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
                description = "Forbidden - only organizers can create rounds"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<List<RoundResponse>> createRoundsForDart(
        @Valid @RequestBody CreateRoundsRequest request
    ) {
        log.info("Creating rounds for dart with ID: {}", request.dartId());
        List<RoundResponse> response = roundService.createRoundsForDart(request);
        log.info("Created {} rounds for dart {}", response.size(), request.dartId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Create a single round.
     *
     * @param dartId the dart ID
     * @param request the round creation request
     * @return the created round
     */
    @PostMapping(
        value = "/dart/{dartId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Create a round",
        description = "Creates a new round for a dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Round created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<RoundResponse> createRound(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Valid @RequestBody RoundRequest request
    ) {
        log.info("Creating round {} for dart {}", request.number(), dartId);
        RoundResponse response = roundService.createRound(dartId, request);
        log.info("Round created successfully with ID: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all rounds for a dart.
     *
     * @param dartId the dart ID
     * @return list of rounds
     */
    @GetMapping(value = "/dart/{dartId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get all rounds for a dart",
        description = "Retrieves all rounds for a specific dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Rounds retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
    public ResponseEntity<List<RoundResponse>> getAllRoundsByDartId(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Fetching all rounds for dart {}", dartId);
        List<RoundResponse> response = roundService.getAllRoundsByDartId(dartId);
        log.info("Retrieved {} rounds for dart {}", response.size(), dartId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all rounds for a dart with pagination.
     *
     * @param dartId the dart ID
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of rounds
     */
    @GetMapping(value = "/dart/{dartId}/paginated", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get rounds for a dart (paginated)",
        description = "Retrieves rounds for a specific dart with pagination"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Rounds retrieved successfully",
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
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<PageResponse<RoundResponse>> getAllRoundsByDartIdPaginated(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Parameter(
            description = "Page number (0-based)",
            example = "0"
        ) @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size", example = "10") @RequestParam(
            defaultValue = "10"
        ) int size
    ) {
        log.info("Fetching rounds for dart {} with pagination - page: {}, size: {}", dartId, page, size);
        PageResponse<RoundResponse> response = roundService.getAllRoundsByDartId(dartId, page, size);
        log.info("Retrieved {} rounds for dart {}", response.getContent().size(), dartId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a round by ID.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @return the round details
     */
    @GetMapping(value = "/dart/{dartId}/{roundId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get round by ID",
        description = "Retrieves detailed information about a specific round"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Round retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Round not found"),
        }
    )
    public ResponseEntity<RoundResponse> getRoundById(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Parameter(
            description = "Unique identifier of the round",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174001"
        ) @PathVariable("roundId") UUID roundId
    ) {
        log.info("Fetching round {} for dart {}", roundId, dartId);
        RoundResponse response = roundService.getRoundById(dartId, roundId);
        log.info("Round retrieved successfully: {}", roundId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get the current (next unpaid) round for a dart.
     *
     * @param dartId the dart ID
     * @return the current round
     */
    @GetMapping(value = "/dart/{dartId}/current", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get current round for a dart",
        description = "Retrieves the current (next unpaid) round for a dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Current round retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Dart or current round not found"),
        }
    )
    public ResponseEntity<RoundResponse> getCurrentRound(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Fetching current round for dart {}", dartId);
        RoundResponse response = roundService.getCurrentRoundByDartId(dartId);
        log.info("Current round retrieved successfully for dart {}", dartId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update a round.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @param request the round update request
     * @return the updated round
     */
    @PutMapping(
        value = "/dart/{dartId}/{roundId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Update a round",
        description = "Updates an existing round's information"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Round updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Round not found"),
        }
    )
    public ResponseEntity<RoundResponse> updateRound(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Parameter(
            description = "Unique identifier of the round",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174001"
        ) @PathVariable("roundId") UUID roundId,
        @Valid @RequestBody RoundRequest request
    ) {
        log.info("Updating round {} for dart {}", roundId, dartId);
        RoundResponse response = roundService.updateRound(dartId, roundId, request);
        log.info("Round updated successfully: {}", roundId);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a round as paid.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @return the updated round
     */
    @PutMapping(
        value = "/dart/{dartId}/{roundId}/mark-paid",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Mark round as paid",
        description = "Marks a round as paid"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Round marked as paid successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RoundResponse.class)
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
            @ApiResponse(responseCode = "404", description = "Round not found"),
        }
    )
    public ResponseEntity<RoundResponse> markRoundAsPaid(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Parameter(
            description = "Unique identifier of the round",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174001"
        ) @PathVariable("roundId") UUID roundId
    ) {
        log.info("Marking round {} as paid for dart {}", roundId, dartId);
        RoundResponse response = roundService.markRoundAsPaid(dartId, roundId);
        log.info("Round marked as paid successfully: {}", roundId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a round.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @return no content
     */
    @DeleteMapping(value = "/dart/{dartId}/{roundId}")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Delete a round",
        description = "Deletes a round"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "204",
                description = "Round deleted successfully"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - insufficient permissions"
            ),
            @ApiResponse(responseCode = "404", description = "Round not found"),
        }
    )
    public ResponseEntity<Void> deleteRound(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Parameter(
            description = "Unique identifier of the round",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174001"
        ) @PathVariable("roundId") UUID roundId
    ) {
        log.info("Deleting round {} for dart {}", roundId, dartId);
        roundService.deleteRound(dartId, roundId);
        log.info("Round deleted successfully: {}", roundId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get round statistics for a dart.
     *
     * @param dartId the dart ID
     * @return round statistics
     */
    @GetMapping(value = "/dart/{dartId}/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get round statistics",
        description = "Retrieves statistics about rounds for a dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Statistics retrieved successfully"
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
    public ResponseEntity<RoundService.RoundStatistics> getRoundStatistics(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Fetching round statistics for dart {}", dartId);
        RoundService.RoundStatistics statistics = roundService.getRoundStatistics(dartId);
        log.info("Round statistics retrieved successfully for dart {}", dartId);
        return ResponseEntity.ok(statistics);
    }
}
