package com.tontin.platform.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Aggregated payment stats for the reports dashboard")
public record PaymentReportSummaryResponse(
    @Schema(description = "Sum of completed (PAYED) contributions in the period") BigDecimal totalContributions,
    @Schema(description = "Sum of pending contribution payments in the period") BigDecimal pendingContributions,
    @Schema(description = "Approximate total received as round recipient (PAYED rounds) in the period") BigDecimal totalPayouts,
    @Schema(description = "Placeholder for overdue logic; may be zero") BigDecimal overdueContributions,
    @Schema(description = "Optional hint for next payout / contribution") NextPaymentDue nextPaymentDue
) {
    public record NextPaymentDue(
        String darId,
        String darName,
        BigDecimal amount,
        String dueDate
    ) {}
}
