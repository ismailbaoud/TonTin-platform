package com.tontin.platform.dto.member.response;

import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.member.MemberStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for Member information.
 *
 * <p>This immutable record represents a member's participation in a dart,
 * including their user information, permission level, status, and associated dart details.</p>
 *
 * @param id          Unique identifier of the member record
 * @param user        Basic information about the user
 * @param permission  Permission level within the dart
 * @param status      Current status of the member
 * @param joinedAt    Timestamp when the member joined the dart
 * @param dart        Basic information about the associated dart
 * @param createdAt   Timestamp when the member record was created
 * @param updatedAt   Timestamp when the member record was last updated
 */
@Builder
@Schema(description = "Response object containing member information")
public record MemberResponse(
    @Schema(
        description = "Unique identifier of the member record",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID id,

    @Schema(description = "User information") UserInfo user,

    @Schema(
        description = "Permission level within the dart",
        example = "MEMBER",
        allowableValues = { "ORGANIZER", "MEMBER" }
    )
    DartPermission permission,

    @Schema(
        description = "Current status of the member",
        example = "ACTIVE",
        allowableValues = { "PENDING", "ACTIVE", "LEAVED" }
    )
    MemberStatus status,

    @Schema(
        description = "Timestamp when the member joined the dart",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime joinedAt,

    @Schema(description = "Dart information") DartInfo dart,

    @Schema(
        description = "Timestamp when the member record was created",
        example = "2024-01-01T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the member record was last updated",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime updatedAt
) {
    /**
     * Nested record for user information.
     *
     * @param id       User unique identifier
     * @param userName Username
     * @param email    User email address
     */
    @Builder
    @Schema(description = "Basic user information")
    public record UserInfo(
        @Schema(
            description = "User unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) UUID id,
        @Schema(description = "Username", example = "john_doe") String userName,
        @Schema(
            description = "User email address",
            example = "john.doe@example.com"
        ) String email
    ) {}

    /**
     * Nested record for dart information.
     *
     * @param id                  Dart unique identifier
     * @param name                Dart name
     * @param monthlyContribution Monthly contribution amount
     */
    @Builder
    @Schema(description = "Basic dart information")
    public record DartInfo(
        @Schema(
            description = "Dart unique identifier",
            example = "123e4567-e89b-12d3-a456-426614174000"
        ) UUID id,
        @Schema(
            description = "Dart name",
            example = "Family Savings Circle"
        ) String name,
        @Schema(
            description = "Monthly contribution amount",
            example = "100.00"
        ) java.math.BigDecimal monthlyContribution
    ) {}

    /**
     * Checks if the member is active.
     *
     * @return true if the member status is ACTIVE
     */
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    /**
     * Checks if the member is pending confirmation.
     *
     * @return true if the member status is PENDING
     */
    public boolean isPending() {
        return status == MemberStatus.PENDING;
    }

    /**
     * Checks if the member has left.
     *
     * @return true if the member status is LEAVED
     */
    public boolean hasLeft() {
        return status == MemberStatus.LEAVED;
    }

    /**
     * Checks if the member is an organizer.
     *
     * @return true if the permission is ORGANIZER
     */
    public boolean isOrganizer() {
        return permission == DartPermission.ORGANIZER;
    }

    /**
     * Checks if the member is a regular member.
     *
     * @return true if the permission is MEMBER
     */
    public boolean isMember() {
        return permission == DartPermission.MEMBER;
    }

    /**
     * Calculates how many days the member has been part of the dart.
     *
     * @return number of days since joining
     */
    public long getDaysSinceJoining() {
        if (joinedAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            joinedAt,
            LocalDateTime.now()
        );
    }
}
