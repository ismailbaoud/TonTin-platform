package com.tontin.platform.dto.dart.response;

import com.tontin.platform.domain.enums.dart.DartStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for Dart (Tontine/Savings Circle) information.
 *
 * <p>
 * This immutable record represents the complete state of a dart,
 * including its configuration, status, and metadata.
 * </p>
 *
 * @param id                  Unique identifier of the dart
 * @param name                Name of the dart
 * @param monthlyContribution Monthly contribution amount per member
 * @param startDate           Date when the dart started or is scheduled to start
 * @param orderMethod         Method used to allocate funds to members
 * @param status              Current status of the dart
 * @param memberCount         Total number of members in the dart
 * @param totalMonthlyPool    Monthly contribution amount per member (same as monthlyContribution)
 * @param organizerId         ID of the dart organizer
 * @param organizerName       Name of the dart organizer
 * @param organizerAvatar     Avatar URL of the organizer
 * @param isOrganizer         Whether the current user is the organizer
 * @param userPermission      Current user's permission level in this dart (ORGANIZER or MEMBER)
 * @param userMemberStatus    Current user's member status (PENDING, ACTIVE, LEAVED)
 * @param currentCycle        Current cycle/round number
 * @param totalCycles         Total number of cycles planned
 * @param nextPayoutDate      Date of the next scheduled payout
 * @param image               Cover image URL for the dart
 * @param customRules         Custom rules and terms for this dart
 * @param createdAt           Timestamp when the dart was created
 * @param updatedAt           Timestamp when the dart was last updated
 */
@Builder
@Schema(description = "Response object containing dart information")
public record DartResponse(
    @Schema(
        description = "Unique identifier of the dart",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID id,

    @Schema(description = "Name of the dart", example = "Family Savings Circle")
    String name,

    @Schema(
        description = "Monthly contribution amount per member",
        example = "100.00"
    )
    BigDecimal monthlyContribution,

    @Schema(
        description = "Date when the dart started or is scheduled to start",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime startDate,

    @Schema(
        description = "Method used to allocate funds to members",
        example = "FIXED_ORDER",
        allowableValues = {
            "FIXED_ORDER", "RANDOM_ONCE", "BIDDING_MODEL", "DYNAMIQUE_RANDOM",
        }
    )
    String orderMethod,

    @Schema(
        description = "Description of the dart",
        example = "A savings circle for the holidays"
    )
    String description,

    @Schema(
        description = "Frequency of payments",
        example = "MONTH",
        allowableValues = { "WEEKLY", "BI-WEEKLY", "MONTH", "QUARTERLY" }
    )
    String paymentFrequency,

    @Schema(
        description = "Current status of the dart",
        example = "ACTIVE",
        allowableValues = { "PENDING", "ACTIVE", "FINISHED" }
    )
    DartStatus status,

    @Schema(description = "Total number of members in the dart", example = "10")
    Integer memberCount,

    @Schema(
        description = "Monthly contribution amount per member",
        example = "100.00"
    )
    BigDecimal totalMonthlyPool,

    @Schema(
        description = "ID of the dart organizer",
        example = "123e4567-e89b-12d3-a456-426614174001"
    )
    UUID organizerId,

    @Schema(description = "Name of the dart organizer", example = "John Doe")
    String organizerName,

    @Schema(description = "Avatar URL of the organizer") String organizerAvatar,

    @Schema(
        description = "Whether the current user is the organizer",
        example = "true"
    )
    Boolean isOrganizer,

    @Schema(
        description = "Current user's permission level in this dart",
        example = "MEMBER",
        allowableValues = { "ORGANIZER", "MEMBER" }
    )
    String userPermission,

    @Schema(
        description = "Current user's member status in this dart",
        example = "ACTIVE",
        allowableValues = { "PENDING", "ACTIVE", "LEAVED" }
    )
    String userMemberStatus,

    @Schema(description = "Current cycle/round number", example = "3")
    Integer currentCycle,

    @Schema(description = "Total number of cycles planned", example = "12")
    Integer totalCycles,

    @Schema(
        description = "Date of the next scheduled payout",
        example = "2024-02-15T10:00:00"
    )
    LocalDateTime nextPayoutDate,

    @Schema(description = "Cover image URL for the dart") String image,

    @Schema(description = "Custom rules and terms for this dart")
    String customRules,

    @Schema(
        description = "Timestamp when the dart was created",
        example = "2024-01-01T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the dart was last updated",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime updatedAt
) {
    /**
     * Checks if the dart is currently active.
     *
     * @return true if the dart status is ACTIVE
     */
    public boolean isActive() {
        return status == DartStatus.ACTIVE;
    }

    /**
     * Checks if the dart is pending activation.
     *
     * @return true if the dart status is PENDING
     */
    public boolean isPending() {
        return status == DartStatus.PENDING;
    }

    /**
     * Checks if the dart has finished.
     *
     * @return true if the dart status is FINISHED
     */
    public boolean isFinished() {
        return status == DartStatus.FINISHED;
    }

    /**
     * Checks if the dart has members.
     *
     * @return true if the dart has at least one member
     */
    public boolean hasMembers() {
        return memberCount != null && memberCount > 0;
    }
}
