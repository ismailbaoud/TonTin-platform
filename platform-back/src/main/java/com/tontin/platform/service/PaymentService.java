package com.tontin.platform.service;

import com.tontin.platform.dto.payment.request.CreatePaymentIntentRequest;
import com.tontin.platform.dto.payment.response.CreatePaymentIntentResponse;
import java.util.UUID;

/**
 * Service for creating Stripe payment intents and handling webhooks.
 */
public interface PaymentService {
    /**
     * Create a Stripe PaymentIntent for the current user's contribution for the current round of the given dart.
     * The current user must be a member (not the recipient of the current round).
     *
     * @param request dart ID
     * @return client secret and payment ID for the frontend
     */
    CreatePaymentIntentResponse createPaymentIntent(
        CreatePaymentIntentRequest request
    );

    /**
     * Handle Stripe webhook (e.g. payment_intent.succeeded). Updates payment status and optionally the round.
     * This is the async path triggered by Stripe's servers.
     *
     * @param payload   raw request body
     * @param signature Stripe-Signature header
     */
    void handleWebhook(String payload, String signature);

    /**
     * Mark payment as completed by Stripe PaymentIntent id (idempotent).
     * Called internally by the webhook handler.
     *
     * @param stripePaymentIntentId Stripe PaymentIntent id
     */
    void markPaymentSucceeded(String stripePaymentIntentId);

    /**
     * Confirm a payment as successfully completed using our internal payment record UUID.
     *
     * <p>This is the synchronous confirmation path: the frontend calls this endpoint
     * immediately after {@code stripe.confirmCardPayment()} resolves successfully,
     * guaranteeing the payment is marked PAYED even when the Stripe webhook has not
     * yet fired (e.g. in local development without the Stripe CLI running).</p>
     *
     * <p>The operation is idempotent — calling it on an already-PAYED payment is a no-op.</p>
     *
     * <p>Security: the caller must be authenticated and must be the owner of the payment
     * (i.e. the payer member must belong to the currently authenticated user).</p>
     *
     * @param paymentId our internal payment record UUID (returned by createPaymentIntent)
     * @throws org.springframework.web.server.ResponseStatusException 404 if the payment is not found,
     *         403 if the caller is not the payer
     */
    void confirmPaymentById(UUID paymentId);
}
