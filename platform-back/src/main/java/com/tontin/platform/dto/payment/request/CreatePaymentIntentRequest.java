package com.tontin.platform.dto.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Request to create a Stripe PaymentIntent for a contribution")
public record CreatePaymentIntentRequest(
    @NotNull(message = "Dart ID is required")
    @Schema(description = "Dart (circle) ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
    UUID dartId
) {}
