package com.tontin.platform.dto.round.request;

import com.tontin.platform.domain.enums.round.RoundStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a Round.
 *
 * <p>
 * This immutable record encapsulates all required information to create
 * a new round or update an existing one.
 * </p>
 *
 * @param number    The round number (must be >= 1)
 * @param status    The status of the round
 * @param date      The date when the round occurs
 * @param amount    The amount for this round (must be positive)
 */
@Schema(description = "Request object for creating or updating a round")
public record RoundRequest(
    @NotNull(message = "Round number is required")
    @Min(value = 1, message = "Round number must be greater than or equal to 1")
    @Schema(
        description = "Round number (1-based)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "1"
    )
    Integer number,

    @NotNull(message = "Round status is required")
    @Schema(
        description = "Status of the round",
        example = "INPAYED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = { "PAYED", "INPAYED" }
    )
    RoundStatus status,

    @NotNull(message = "Round date is required")
    @Schema(
        description = "Date when the round occurs",
        example = "2024-02-15T10:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    LocalDateTime date,

    @NotNull(message = "Round amount is required")
    @Positive(message = "Round amount must be positive")
    @Schema(
        description = "Amount for this round",
        example = "1000.00",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0.01"
    )
    BigDecimal amount
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
