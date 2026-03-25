package com.tontin.platform.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single payment row for reports / history")
public record PaymentReportItemResponse(
    @Schema(description = "Internal payment UUID") String id,
    @Schema(description = "Dart UUID") String dartId,
    @Schema(description = "Dart display name") String darName,
    @Schema(description = "Payer member UUID") String payerMemberId,
    @Schema(description = "Payer user UUID") String payerUserId,
    @Schema(description = "Payer username") String payerUserName,
    @Schema(description = "Payer email") String payerEmail,
    @Schema(description = "Amount") double amount,
    @Schema(description = "Frontend-friendly status") String status,
    @Schema(description = "Payment kind") String type,
    @Schema(description = "ISO-8601 payment timestamp") String createdDate
) {}
