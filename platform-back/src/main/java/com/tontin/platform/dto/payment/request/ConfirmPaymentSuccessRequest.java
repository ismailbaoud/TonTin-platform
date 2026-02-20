package com.tontin.platform.dto.payment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Request to confirm that a Stripe payment succeeded (e.g. after confirmCardPayment on frontend).
 * Allows marking the payment as PAYED without waiting for the webhook.
 */
@Builder
@Schema(description = "Request to confirm payment success by Stripe PaymentIntent id")
public record ConfirmPaymentSuccessRequest(
    @NotBlank(message = "PaymentIntent id is required")
    @Schema(description = "Stripe PaymentIntent id (e.g. pi_xxx)", example = "pi_3ABC123xyz", requiredMode = Schema.RequiredMode.REQUIRED)
    String paymentIntentId
) {}
