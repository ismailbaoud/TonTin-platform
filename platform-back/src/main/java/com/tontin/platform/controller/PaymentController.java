package com.tontin.platform.controller;

import com.tontin.platform.dto.payment.request.CreatePaymentIntentRequest;
import com.tontin.platform.dto.payment.response.CreatePaymentIntentResponse;
import com.tontin.platform.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
