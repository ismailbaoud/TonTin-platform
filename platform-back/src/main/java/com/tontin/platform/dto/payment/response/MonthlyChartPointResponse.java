package com.tontin.platform.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "One bar in the contribution history chart")
public record MonthlyChartPointResponse(
    @Schema(description = "Label e.g. Jan 2025") String month,
    @Schema(description = "Total PAYED contributions in that month") BigDecimal value
) {}
