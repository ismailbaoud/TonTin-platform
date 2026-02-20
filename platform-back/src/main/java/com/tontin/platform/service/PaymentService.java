package com.tontin.platform.service;

import com.tontin.platform.dto.payment.request.CreatePaymentIntentRequest;
import com.tontin.platform.dto.payment.response.CreatePaymentIntentResponse;
import java.util.List;
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
    CreatePaymentIntentResponse createPaymentIntent(CreatePaymentIntentRequest request);

    /**
     * Handle Stripe webhook (e.g. payment_intent.succeeded). Updates payment status and optionally the round.
     *
     * @param payload   raw request body
     * @param signature Stripe-Signature header
     */
    void handleWebhook(String payload, String signature);

    /**
     * Mark payment as completed by Stripe PaymentIntent id (idempotent).
     *
     * @param stripePaymentIntentId Stripe PaymentIntent id
     */
    void markPaymentSucceeded(String stripePaymentIntentId);

    /**
     * Get member IDs (payers) who have paid for the given round.
     *
     * @param roundId round id
     * @return list of payer member IDs with status PAYED
     */
    List<UUID> getPaidPayerMemberIdsForRound(UUID roundId);
}
