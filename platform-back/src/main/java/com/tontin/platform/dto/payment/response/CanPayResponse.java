package com.tontin.platform.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "Response indicating whether the current user can access the payment page for a dart")
public record CanPayResponse(
    @Schema(description = "True if the user is allowed to pay (member, not recipient, payment window open)", example = "true")
    boolean canPay
) {}
