package com.tontin.platform.dto.round.response;

import com.tontin.platform.domain.enums.round.RoundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for Round information.
 *
 * <p>
 * This immutable record represents the complete state of a round,
 * including its number, status, date, amount, and associated dart.
 * </p>
 *
 * @param id          Unique identifier of the round
 * @param number      Round number (1-based)
 * @param status      Current status of the round
 * @param date        Date when the round occurs
 * @param amount      Amount for this round
 * @param dartId      ID of the associated dart
 * @param dartName    Name of the associated dart
 * @param createdAt   Timestamp when the round was created
 * @param updatedAt   Timestamp when the round was last updated
 */
@Builder
@Schema(description = "Response object containing round information")
public record RoundResponse(
    @Schema(
        description = "Unique identifier of the round",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID id,

    @Schema(
        description = "Round number (1-based)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Integer number,

    @Schema(
        description = "Current status of the round",
        example = "INPAYED",
        allowableValues = { "PAYED", "INPAYED" }
    )
    RoundStatus status,

    @Schema(
        description = "Date when the round occurs",
        example = "2024-02-15T10:00:00"
    )
    LocalDateTime date,

    @Schema(
        description = "Amount for this round",
        example = "1000.00"
    )
    BigDecimal amount,

    @Schema(
        description = "ID of the associated dart",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID dartId,

    @Schema(
        description = "Name of the associated dart",
        example = "Family Savings Circle"
    )
    String dartName,

    @Schema(
        description = "ID of the member who receives money in this round",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    UUID recipientMemberId,

    @Schema(
        description = "Name of the member who receives money in this round",
        example = "John Doe"
    )
    String recipientMemberName,

    @Schema(
        description = "Email of the member who receives money in this round",
        example = "john.doe@example.com"
    )
    String recipientMemberEmail,

    @Schema(
        description = "Timestamp when the round was created",
        example = "2024-01-15T10:00:00"
    )
    LocalDateTime createdAt,

    @Schema(
        description = "Timestamp when the round was last updated",
        example = "2024-01-15T10:30:00"
    )
    LocalDateTime updatedAt
) {
    /**
     * Checks if the round is paid.
     *
     * @return true if the round status is PAYED
     */
    public boolean isPayed() {
        return status == RoundStatus.PAYED;
    }

    /**
     * Checks if the round is pending payment.
     *
     * @return true if the round status is INPAYED
     */
    public boolean isInPayed() {
        return status == RoundStatus.INPAYED;
    }
}
 