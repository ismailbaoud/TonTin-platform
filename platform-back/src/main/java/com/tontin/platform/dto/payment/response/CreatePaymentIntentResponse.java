package com.tontin.platform.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;

@Builder
@Schema(description = "Stripe client secret and payment record id for confirming payment on the frontend")
public record CreatePaymentIntentResponse(
    @Schema(description = "Stripe PaymentIntent client secret for Stripe.js", example = "pi_xxx_secret_xxx")
    String clientSecret,
    @Schema(description = "Our payment record ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID paymentId
) {}
