package com.tontin.platform.service.impl;

import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import com.tontin.platform.domain.enums.round.RoundStatus;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.payment.response.MonthlyChartPointResponse;
import com.tontin.platform.dto.payment.response.PaymentReportItemResponse;
import com.tontin.platform.dto.payment.response.PaymentReportSummaryResponse;
import com.tontin.platform.repository.PaymentRepository;
import com.tontin.platform.repository.RoundRepository;
import com.tontin.platform.repository.spec.PaymentSpecifications;
import com.tontin.platform.service.PaymentReportService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentReportServiceImpl implements PaymentReportService {

    private final PaymentRepository paymentRepository;
    private final RoundRepository roundRepository;

    @Override
    @Transactional(readOnly = true)
    public PaymentReportSummaryResponse getSummary(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        BigDecimal contributed =
            sumByStatus(userId, dartId, PaymentStatus.PAYED, from, to);
        BigDecimal pending =
            sumByStatus(userId, dartId, PaymentStatus.PENDING, from, to);

        Double payoutRaw =
            userId == null && dartId == null
                ? roundRepository.sumPayoutAmount(
                    RoundStatus.PAYED,
                    from,
                    to
                )
                : userId == null
                    ? roundRepository.sumPayoutAmountForDart(
                        dartId,
                        RoundStatus.PAYED,
                        from,
                        to
                    )
                    : dartId == null
                        ? roundRepository.sumPayoutAmountForRecipient(
                            userId,
                            RoundStatus.PAYED,
                            from,
                            to
                        )
                        : roundRepository.sumPayoutAmountForRecipientAndDart(
                            userId,
                            dartId,
                            RoundStatus.PAYED,
                            from,
                            to
                        );
        BigDecimal payouts = BigDecimal.valueOf(payoutRaw != null ? payoutRaw : 0.0);

        return new PaymentReportSummaryResponse(
            contributed,
            pending,
            payouts,
            BigDecimal.ZERO,
            null
        );
    }

    private BigDecimal sumByStatus(
        UUID userId,
        UUID dartId,
        PaymentStatus status,
        LocalDateTime from,
        LocalDateTime to
    ) {
        if (userId == null && dartId == null) {
            return paymentRepository.sumAmountByStatusAndDateRange(
                status,
                from,
                to
            );
        }
        if (userId == null) {
            return paymentRepository.sumAmountByDartStatusAndDateRange(
                dartId,
                status,
                from,
                to
            );
        }
        if (dartId == null) {
            return paymentRepository.sumAmountByUserStatusAndDateRange(
                userId,
                status,
                from,
                to
            );
        }
        return paymentRepository.sumAmountByUserDartStatusAndDateRange(
            userId,
            dartId,
            status,
            from,
            to
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentReportItemResponse> getPayments(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to,
        PaymentStatus statusFilter,
        int page,
        int size
    ) {
        Specification<Payment> spec =
            Specification.where(PaymentSpecifications.forUser(userId))
                .and(PaymentSpecifications.forDartId(dartId))
                .and(
                    PaymentSpecifications.betweenInclusiveStartExclusiveEnd(from, to)
                )
                .and(PaymentSpecifications.hasStatus(statusFilter));

        Page<Payment> result =
            paymentRepository.findAll(
                spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "date"))
            );

        Page<PaymentReportItemResponse> mapped = result.map(this::toItem);
        return PageResponse.of(mapped);
    }

    private PaymentReportItemResponse toItem(Payment p) {
        return new PaymentReportItemResponse(
            p.getId().toString(),
            p.getRound().getDart().getId().toString(),
            p.getRound().getDart().getName(),
            p.getPayer() != null ? p.getPayer().getId().toString() : null,
            p.getPayer() != null && p.getPayer().getUser() != null
                ? p.getPayer().getUser().getId().toString()
                : null,
            p.getPayer() != null && p.getPayer().getUser() != null
                ? p.getPayer().getUser().getUserName()
                : null,
            p.getPayer() != null && p.getPayer().getUser() != null
                ? p.getPayer().getUser().getEmail()
                : null,
            p.getAmount().doubleValue(),
            toFrontendStatus(p.getPaymentStatus()),
            "contribution",
            p.getDate().toString()
        );
    }

    private static String toFrontendStatus(PaymentStatus s) {
        return switch (s) {
            case PAYED -> "completed";
            case PENDING -> "pending";
            case CANCELLED -> "cancelled";
        };
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyChartPointResponse> getMonthlyChart(
        UUID userId,
        UUID dartId,
        LocalDateTime from,
        LocalDateTime to
    ) {
        List<Object[]> rows =
            dartId == null
                ? paymentRepository.aggregateMonthlyPayedContributionsAllDarts(
                    userId,
                    from,
                    to
                )
                : paymentRepository.aggregateMonthlyPayedContributionsForDart(
                    userId,
                    from,
                    to,
                    dartId
                );

        Map<YearMonth, BigDecimal> totals = new HashMap<>();
        for (Object[] row : rows) {
            if (row[0] == null || row[1] == null) {
                continue;
            }
            LocalDateTime bucket =
                row[0] instanceof Timestamp ts
                    ? ts.toLocalDateTime()
                    : (LocalDateTime) row[0];
            YearMonth ym = YearMonth.from(bucket);
            BigDecimal amt =
                row[1] instanceof BigDecimal bd
                    ? bd
                    : BigDecimal.valueOf(((Number) row[1]).doubleValue());
            totals.put(ym, amt);
        }

        YearMonth startYm = YearMonth.from(from);
        YearMonth endYm = YearMonth.from(to.minusNanos(1));

        List<MonthlyChartPointResponse> out = new ArrayList<>();
        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            String label =
                ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.US)
                    + " "
                    + ym.getYear();
            BigDecimal value = totals.getOrDefault(ym, BigDecimal.ZERO);
            out.add(new MonthlyChartPointResponse(label, value));
        }
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportCsv(UUID userId, UUID dartId, LocalDateTime from, LocalDateTime to) {
        Specification<Payment> spec =
            Specification.where(PaymentSpecifications.forUser(userId))
                .and(PaymentSpecifications.forDartId(dartId))
                .and(
                    PaymentSpecifications.betweenInclusiveStartExclusiveEnd(from, to)
                );

        Sort sort = Sort.by(Sort.Direction.DESC, "date");
        List<Payment> all = new ArrayList<>();
        int pageIdx = 0;
        Page<Payment> page;
        do {
            page =
                paymentRepository.findAll(
                    spec,
                    PageRequest.of(pageIdx++, 500, sort)
                );
            all.addAll(page.getContent());
        } while (page.hasNext());

        StringBuilder sb = new StringBuilder();
        sb.append("id,dartId,darName,date,amount,status,type\n");
        for (Payment p : all) {
            sb.append(csvEscape(p.getId().toString()))
                .append(',')
                .append(csvEscape(p.getRound().getDart().getId().toString()))
                .append(',')
                .append(csvEscape(p.getRound().getDart().getName()))
                .append(',')
                .append(csvEscape(p.getDate().toString()))
                .append(',')
                .append(p.getAmount().toPlainString())
                .append(',')
                .append(csvEscape(toFrontendStatus(p.getPaymentStatus())))
                .append(',')
                .append("contribution")
                .append('\n');
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static String csvEscape(String s) {
        if (s == null) {
            return "";
        }
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
