package com.tontin.platform.dto.round.response;

import com.tontin.platform.domain.enums.round.RoundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
/** Number of days before the round date that payments are allowed to open. */
import lombok.Builder;

/**
 * Response DTO for Round information.
 *
 * <p>
 * This immutable record represents the complete state of a round,
 * including its number, status, date, amount, and associated dart.
 * </p>
 *
 * @param id                    Unique identifier of the round
 * @param number                Round number (1-based)
 * @param status                Current status of the round
 * @param date                  Date when the round occurs
 * @param amount                Amount for this round
 * @param dartId                ID of the associated dart
 * @param dartName              Name of the associated dart
 * @param recipientMemberId     Member ID of the recipient
 * @param recipientMemberName   Name of the recipient member
 * @param recipientMemberEmail  Email of the recipient member
 * @param paidMemberIds         List of member IDs (payers) who have already paid for this round
 * @param paymentOpenDate       Date from which payments are accepted (round date minus PAYMENT_WINDOW_DAYS)
 * @param createdAt             Timestamp when the round was created
 * @param updatedAt             Timestamp when the round was last updated
 */
/** Number of days before the round date that payments are allowed to open. */
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

    @Schema(description = "Amount for this round", example = "1000.00")
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
        description = "List of member IDs (payers) who have already paid their contribution for this round"
    )
    List<UUID> paidMemberIds,

    @Schema(
        description = "Earliest date from which members are allowed to submit their payment for this round (round date minus 5 days). Null if round date is not set.",
        example = "2026-01-27T00:00:00"
    )
    LocalDateTime paymentOpenDate,

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

    /** Number of days before the round date that payments open. */
    public static final int PAYMENT_WINDOW_DAYS = 5;

    /**
     * Checks if a specific member has already paid for this round.
     *
     * @param memberId the member ID to check
     * @return true if the member has paid
     */
    public boolean hasMemberPaid(UUID memberId) {
        if (paidMemberIds == null || memberId == null) return false;
        return paidMemberIds.contains(memberId);
    }

    /**
     * Returns true when the payment window is currently open,
     * i.e. now is on or after {@code paymentOpenDate}.
     */
    public boolean isPaymentWindowOpen() {
        if (paymentOpenDate == null) return false;
        return !LocalDateTime.now().isBefore(paymentOpenDate);
    }
}
