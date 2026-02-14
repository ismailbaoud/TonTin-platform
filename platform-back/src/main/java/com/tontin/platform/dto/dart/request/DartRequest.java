package com.tontin.platform.dto.dart.request;

import com.tontin.platform.domain.enums.round.OrderMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a Dart (Tontine/Savings Circle).
 *
 * <p>
 * This immutable record encapsulates all required information to create
 * a new dart or update an existing one.
 * </p>
 *
 * @param name                The name of the dart (3-100 characters)
 * @param monthlyContribution The monthly contribution amount (must be positive)
 * @param allocationMethod    The method used to allocate funds to members
 */
@Schema(description = "Request object for creating or updating a dart")
public record DartRequest(
    @NotNull(message = "Dart name cannot be null")
    @NotBlank(message = "Dart name is required")
    @Size(
        min = 3,
        max = 100,
        message = "Dart name must be between 3 and 100 characters"
    )
    @Schema(
        description = "Name of the dart",
        example = "Family Savings Circle",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minLength = 3,
        maxLength = 100
    )
    String name,

    @NotNull(message = "Monthly contribution is required")
    @DecimalMin(
        value = "0.01",
        message = "Monthly contribution must be greater than zero"
    )
    @Schema(
        description = "Monthly contribution amount per member",
        example = "100.00",
        requiredMode = Schema.RequiredMode.REQUIRED,
        minimum = "0.01"
    )
    BigDecimal monthlyContribution,

    @NotNull(message = "Order method cannot be null")
    @Schema(
        description = "Method used to allocate funds (e.g., FIXED_ORDER, RANDOM_ONCE, BIDDING_MODEL, DYNAMIQUE_RANDOM)",
        example = "FIXED_ORDER",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {
            "FIXED_ORDER", "RANDOM_ONCE", "BIDDING_MODEL", "DYNAMIQUE_RANDOM",
        }
    )
    OrderMethod orderMethod,

    @Nullable
    @Schema(
        description = "custom rules of the dart",
        example = "1: you should pay on time"
    )
    String customRules,

    @Nullable
    @Size(max = 500, message = "Description must be less than 500 characters")
    @Schema(
        description = "Description of the dart",
        example = "A savings circle for the holidays"
    )
    String description,

    @NotNull(message = "Payment Frequency cannot be null")
    @NotBlank(message = "Payment Frequency is required")
    @Size(
        min = 3,
        max = 50,
        message = "Payment Frequency must be between 3 and 50 characters"
    )
    @Schema(
        description = "Payment Frequency used to allocate funds (e.g., Weekly, Bi-Weekly, Month, Quarterly)",
        example = "MONTH",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = { "WEEKLY", "BI-WEEKLY", "MONTH", "QUARTERLY" }
    )
    String paymentFrequency
) {
    /**
     * Compact constructor for additional validation.
     */
    public DartRequest {
        // Trim strings
        if (name != null) {
            name = name.trim();
        }
    }

    /**
     * Validates if the allocation method is supported.
     *
     * @return true if the allocation method is valid
     */
    public boolean isValidAllocationMethod() {
        return (
            orderMethod != null &&
            (orderMethod == OrderMethod.FIXED_ORDER ||
                orderMethod == OrderMethod.RANDOM_ONCE ||
                orderMethod == OrderMethod.BIDDING_MODEL ||
                orderMethod == OrderMethod.DYNAMIQUE_RANDOM)
        );
    }
}
