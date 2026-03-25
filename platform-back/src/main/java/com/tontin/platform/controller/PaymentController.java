package com.tontin.platform.controller;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.payment.request.CreatePaymentIntentRequest;
import com.tontin.platform.dto.payment.response.CanPayResponse;
import com.tontin.platform.dto.payment.response.CreatePaymentIntentResponse;
import com.tontin.platform.dto.payment.response.MonthlyChartPointResponse;
import com.tontin.platform.dto.payment.response.PaymentReportItemResponse;
import com.tontin.platform.dto.payment.response.PaymentReportSummaryResponse;
import com.tontin.platform.service.PaymentReportService;
import com.tontin.platform.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(
    name = "Payments",
    description = "Stripe contribution payments and webhooks"
)
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentReportService paymentReportService;
    private final SecurityUtils securityUtils;

    @Value("${stripe.publishable-key:}")
    private String stripePublishableKey;

    @GetMapping(value = "/config/stripe-public", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Stripe publishable key (safe to expose in the browser)")
    public ResponseEntity<Map<String, String>> stripePublicConfig() {
        String key = stripePublishableKey != null ? stripePublishableKey : "";
        return ResponseEntity.ok(Map.of("publishableKey", key));
    }

    private static LocalDateTime[] resolveDateRange(LocalDate start, LocalDate end) {
        LocalDate s = start != null ? start : LocalDate.now().withDayOfMonth(1);
        LocalDate e = end != null ? end : LocalDate.now();
        return new LocalDateTime[] { s.atStartOfDay(), e.plusDays(1).atStartOfDay() };
    }

    private static PaymentStatus parsePaymentStatusFilter(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return switch (raw.trim().toLowerCase(Locale.ROOT)) {
            case "completed", "payed" -> PaymentStatus.PAYED;
            case "pending" -> PaymentStatus.PENDING;
            case "cancelled" -> PaymentStatus.CANCELLED;
            case "overdue" -> PaymentStatus.PENDING;
            default -> null;
        };
    }

    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Payment report summary for current user")
    public ResponseEntity<PaymentReportSummaryResponse> getReportSummary(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate
    ) {
        UUID userId = securityUtils.requireCurrentUserId();
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        return ResponseEntity.ok(
            paymentReportService.getSummary(userId, dartId, range[0], range[1])
        );
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Paginated payments for current user (reports)")
    public ResponseEntity<PageResponse<PaymentReportItemResponse>> getMyPaymentsForReport(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        UUID userId = securityUtils.requireCurrentUserId();
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        PaymentStatus st = parsePaymentStatusFilter(status);
        return ResponseEntity.ok(
            paymentReportService.getPayments(
                userId,
                dartId,
                range[0],
                range[1],
                st,
                page,
                size
            )
        );
    }

    @GetMapping(value = "/admin/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Global payment report summary for admin")
    public ResponseEntity<PaymentReportSummaryResponse> getAdminReportSummary(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate
    ) {
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        return ResponseEntity.ok(
            paymentReportService.getSummary(null, dartId, range[0], range[1])
        );
    }

    @GetMapping(value = "/admin/list", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Global paginated payments for admin")
    public ResponseEntity<PageResponse<PaymentReportItemResponse>> getAdminPaymentsForReport(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate,
        @RequestParam(required = false) String status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        PaymentStatus st = parsePaymentStatusFilter(status);
        return ResponseEntity.ok(
            paymentReportService.getPayments(
                null,
                dartId,
                range[0],
                range[1],
                st,
                page,
                size
            )
        );
    }

    @GetMapping(value = "/chart/monthly", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Monthly contribution totals for charts")
    public ResponseEntity<List<MonthlyChartPointResponse>> getMonthlyChart(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate
    ) {
        UUID userId = securityUtils.requireCurrentUserId();
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        return ResponseEntity.ok(
            paymentReportService.getMonthlyChart(userId, dartId, range[0], range[1])
        );
    }

    @GetMapping(value = "/export", produces = "text/csv")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Export payments as CSV (filtered)")
    public ResponseEntity<byte[]> exportPaymentsCsv(
        @RequestParam(required = false) UUID dartId,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate startDate,
        @RequestParam(required = false) @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
        ) LocalDate endDate
    ) {
        UUID userId = securityUtils.requireCurrentUserId();
        LocalDateTime[] range = resolveDateRange(startDate, endDate);
        byte[] bytes =
            paymentReportService.exportCsv(userId, dartId, range[0], range[1]);
        return ResponseEntity.ok()
            .header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"payment-report.csv\""
            )
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    @GetMapping(value = "/can-pay", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Check if current user can access payment page",
        description = "Returns whether the current user is allowed to pay for the given dart (member, not recipient, payment window open). Use this to guard the pay-contribution page."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Check result",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CanPayResponse.class)
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "No current round, or user is recipient, or payment not open yet"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Not a member of the dart"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<CanPayResponse> canPay(
        @RequestParam(name = "dartId") UUID dartId
    ) {
        CanPayResponse response = paymentService.canPay(dartId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
        value = "/create-intent",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Create payment intent",
        description = "Creates a Stripe PaymentIntent for the current user's contribution for the current round of the dart. Returns clientSecret for Stripe.js."
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Payment intent created",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(
                        implementation = CreatePaymentIntentResponse.class
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid request or user is recipient"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Not a member of the dart"
            ),
            @ApiResponse(responseCode = "404", description = "Dart not found"),
        }
    )
    public ResponseEntity<CreatePaymentIntentResponse> createPaymentIntent(
        @Valid @RequestBody CreatePaymentIntentRequest request
    ) {
        CreatePaymentIntentResponse response =
            paymentService.createPaymentIntent(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Synchronous client-side confirmation endpoint.
     *
     * <p>The frontend calls this immediately after {@code stripe.confirmCardPayment()}
     * resolves successfully. This guarantees the payment record is marked PAYED
     * without depending on the Stripe webhook (which may not fire in local dev).</p>
     *
     * <p>The operation is idempotent — confirming an already-PAYED payment is a no-op.</p>
     */
    @PostMapping(value = "/{paymentId}/confirm-success")
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(
        summary = "Confirm payment success (client-side)",
        description = """
        Called by the frontend immediately after stripe.confirmCardPayment() resolves \
        successfully. Marks the payment as PAYED and closes the round when all members \
        have contributed. This is the reliable synchronous path that works even when the \
        Stripe webhook has not yet fired (e.g. in local development).\
        The operation is idempotent.\
        """
    )
    @ApiResponses(
        value = {
            @ApiResponse(
                responseCode = "200",
                description = "Payment confirmed successfully"
            ),
            @ApiResponse(
                responseCode = "403",
                description = "Caller is not the owner of this payment"
            ),
            @ApiResponse(
                responseCode = "404",
                description = "Payment not found"
            ),
        }
    )
    public ResponseEntity<Map<String, String>> confirmPaymentSuccess(
        @PathVariable UUID paymentId
    ) {
        log.info(
            "Client-side payment confirmation for paymentId: {}",
            paymentId
        );
        paymentService.confirmPaymentById(paymentId);
        return ResponseEntity.ok(Map.of("status", "PAYED"));
    }

    @PostMapping(
        value = "/webhook",
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @Operation(
        summary = "Stripe webhook",
        description = "Stripe sends events here (e.g. payment_intent.succeeded). Do not call directly; configure this URL in Stripe Dashboard."
    )
    @ApiResponses(
        value = {
            @ApiResponse(responseCode = "200", description = "Event processed"),
            @ApiResponse(
                responseCode = "400",
                description = "Invalid signature or payload"
            ),
        }
    )
    public ResponseEntity<Map<String, String>> webhook(
        @RequestBody String payload,
        @RequestHeader(
            value = "Stripe-Signature",
            required = false
        ) String signature
    ) {
        paymentService.handleWebhook(
            payload,
            signature != null ? signature : ""
        );
        return ResponseEntity.ok(Map.of("received", "true"));
    }
}
