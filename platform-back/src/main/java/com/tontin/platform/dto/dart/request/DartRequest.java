package com.tontin.platform.dto.dart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request DTO for creating or updating a Dart (Tontine/Savings Circle).
 *
 * <p>This immutable record encapsulates all required information to create
 * a new dart or update an existing one.</p>
 *
 * @param name                 The name of the dart (3-100 characters)
 * @param monthlyContribution  The monthly contribution amount (must be positive)
 * @param allocationMethod     The method used to allocate funds to members
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

    @NotNull(message = "Allocation method cannot be null")
    @NotBlank(message = "Allocation method is required")
    @Size(
        min = 3,
        max = 50,
        message = "Allocation method must be between 3 and 50 characters"
    )
    @Schema(
        description = "Method used to allocate funds (e.g., RANDOM, ROUND_ROBIN, AUCTION)",
        example = "RANDOM",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = { "RANDOM", "ROUND_ROBIN", "AUCTION", "FIXED_ORDER" }
    )
    String allocationMethod
) {
    /**
     * Compact constructor for additional validation.
     */
    public DartRequest {
        // Trim strings
        if (name != null) {
            name = name.trim();
        }
        if (allocationMethod != null) {
            allocationMethod = allocationMethod.trim().toUpperCase();
        }
    }

    /**
     * Validates if the allocation method is supported.
     *
     * @return true if the allocation method is valid
     */
    public boolean isValidAllocationMethod() {
        return (
            allocationMethod != null &&
            (allocationMethod.equals("RANDOM") ||
                allocationMethod.equals("ROUND_ROBIN") ||
                allocationMethod.equals("AUCTION") ||
                allocationMethod.equals("FIXED_ORDER"))
        );
    }
}
