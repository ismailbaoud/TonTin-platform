package com.tontin.platform.controller;

import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import com.tontin.platform.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
 * REST Controller for Member management operations.
 *
 * <p>
 * This controller handles operations related to dart membership, including
 * adding members, updating permissions, and managing member status. Requires
 * authentication with CLIENT or ADMIN role.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Member Management",
    description = "Endpoints for managing dart members and their permissions"
)
@SecurityRequirement(name = "Bearer Authentication")
public class MemberController {

    private final MemberService memberService;

    /**
     * Adds a new member to a dart.
     *
     * @param userId  the unique identifier of the user to add as member
     * @param dartId  the unique identifier of the dart
     * @param request the member permission request
     * @return the created member details
     */
    @PostMapping(
        value = "/dart/{dartId}/user/{userId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Add member to dart",
        description = "Adds a user as a member to a specific dart with the specified permission level"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "201",
                description = "Member added successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponse.class)
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
                description = "Forbidden - only organizers can add members"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Dart or user not found"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - user is already a member of this dart"
            ),
        }
    )
    public ResponseEntity<MemberResponse> addMember(
        @Parameter(
            description = "Unique identifier of the user to add",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("userId") UUID userId,
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Valid @RequestBody MemberRequest request
    ) {
        log.info("Adding user {} as member to dart {}", userId, dartId);
        MemberResponse response = memberService.addMemberToDart(
            request,
            userId,
            dartId
        );
        log.info("Member added successfully: {}", response.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates a member's permission level.
     *
     * @param memberId the unique identifier of the member
     * @param dartId   the unique identifier of the dart
     * @param request  the member permission update request
     * @return the updated member details
     */
    @PutMapping(
        value = "/{memberId}/dart/{dartId}",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Update member permission",
        description = "Updates a member's permission level within a dart. Only organizers can update permissions."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Member permission updated successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponse.class)
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
                description = "Forbidden - only organizers can update permissions"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Member or dart not found"
            ),
        }
    )
    public ResponseEntity<MemberResponse> updateMemberPermission(
        @Parameter(
            description = "Unique identifier of the member",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("memberId") UUID memberId,
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId,
        @Valid @RequestBody MemberRequest request
    ) {
        log.info(
            "Updating permission for member {} in dart {}",
            memberId,
            dartId
        );
        MemberResponse response = memberService.updateMemberPermission(
            request,
            memberId,
            dartId
        );
        log.info("Member permission updated successfully: {}", memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a member by ID within a specific dart.
     *
     * @param memberId the unique identifier of the member
     * @param dartId   the unique identifier of the dart
     * @return the member details
     */
    @GetMapping(
        value = "/{memberId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get member by ID",
        description = "Retrieves detailed information about a specific member within a dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Member retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponse.class)
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
            @ApiResponse(
                responseCode = "404",
                description = "Member or dart not found"
            ),
        }
    )
    public ResponseEntity<MemberResponse> getMember(
        @Parameter(
            description = "Unique identifier of the member",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("memberId") UUID memberId,
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @RequestParam("dartId") UUID dartId
    ) {
        log.info("Fetching member {} from dart {}", memberId, dartId);
        MemberResponse response = memberService.getMember(memberId, dartId);
        log.info("Member retrieved successfully: {}", memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all members of a specific dart.
     *
     * @param dartId the unique identifier of the dart
     * @return list of all members in the dart
     */
    @GetMapping(
        value = "/dart/{dartId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Get all members of a dart",
        description = "Retrieves a list of all members belonging to a specific dart"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Members retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(
                        schema = @Schema(implementation = MemberResponse.class)
                    )
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
    public ResponseEntity<List<MemberResponse>> getAllMembers(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Fetching all members for dart {}", dartId);
        List<MemberResponse> response = memberService.getAllMembersOfDart(
            dartId
        );
        log.info("Retrieved {} members for dart {}", response.size(), dartId);
        return ResponseEntity.ok(response);
    }

    /**
     * Removes a member from a dart.
     *
     * @param memberId the unique identifier of the member to remove
     * @param dartId   the unique identifier of the dart
     * @return success message
     */
    @DeleteMapping(
        value = "/{memberId}",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Remove member from dart",
        description = "Removes a member from a dart. Only organizers can remove members."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Member removed successfully"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Forbidden - only organizers can remove members"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Member or dart not found"
            ),
            @ApiResponse(
                responseCode = "409",
                description = "Conflict - cannot remove the last organizer"
            ),
        }
    )
    public ResponseEntity<MessageResponse> deleteMember(
        @Parameter(
            description = "Unique identifier of the member",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("memberId") UUID memberId,
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @RequestParam("dartId") UUID dartId
    ) {
        log.info("Removing member {} from dart {}", memberId, dartId);
        String message = memberService.deleteMember(memberId, dartId);
        log.info("Member removed successfully: {}", memberId);
        return ResponseEntity.ok(new MessageResponse(message));
    }

    /**
     * Accept invitation to join a dart (change member status from PENDING to ACTIVE)
     *
     * @param dartId the unique identifier of the dart
     * @return the updated member details
     */
    @PostMapping(
        value = "/dart/{dartId}/accept",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Accept invitation to dart",
        description = "Accepts an invitation to join a dart by changing member status from PENDING to ACTIVE"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Invitation accepted successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invitation has already been processed or invalid status"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Member not found in this dart"
            ),
        }
    )
    public ResponseEntity<MemberResponse> acceptInvitation(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Accepting invitation for dart {}", dartId);
        MemberResponse response = memberService.acceptInvitation(dartId);
        log.info("Invitation accepted successfully for dart {}", dartId);
        return ResponseEntity.ok(response);
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
            example = "Member removed successfully"
        ) String message
    ) {}

    /**
     * rejecte invitation to join a dart (change member status from PENDING to LEAVED)
     * 
     * @param dartId the unique identifier of the dart
     * @return the updated member details
     */
    @PostMapping(
        value = "/dart/{dartId}/leave",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(
        summary = "Reject invetation to dart",
        description = "Reject an invitation to join a dart by changing member status from PENDING to LEAVED"
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Invitation rejected successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MemberResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invitation has already been processed or invalid status"
            ),
            @ApiResponse(
                responseCode = "401",
                description = "Unauthorized - authentication required"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Member not found in this dart"
            ),
        }
    )
    public ResponseEntity<MemberResponse> rejectInvitation(
        @Parameter(
            description = "Unique identifier of the dart",
            required = true,
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) @PathVariable("dartId") UUID dartId
    ) {
        log.info("Rejecting invitation for dart {}", dartId);
        MemberResponse response = memberService.rejectInvitation(dartId);
        log.info("Invitation accepted successfully for dart {}", dartId);
        return ResponseEntity.ok(response);
    }
}
