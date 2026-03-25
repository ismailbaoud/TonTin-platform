package com.tontin.platform.service;

import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.payment.response.MonthlyChartPointResponse;
import com.tontin.platform.dto.payment.response.PaymentReportItemResponse;
import com.tontin.platform.dto.payment.response.PaymentReportSummaryResponse;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaymentReportService {

    PaymentReportSummaryResponse getSummary(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to
    );

    PageResponse<PaymentReportItemResponse> getPayments(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to,
        PaymentStatus statusFilter,
        int page,
        int size
    );

    List<MonthlyChartPointResponse> getMonthlyChart(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to
    );

    byte[] exportCsv(UUID userId, UUID dartId, LocalDateTime from, LocalDateTime to);
}
