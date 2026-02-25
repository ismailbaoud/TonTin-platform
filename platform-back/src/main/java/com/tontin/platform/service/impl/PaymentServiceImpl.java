package com.tontin.platform.service.impl;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.Round;
import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import com.tontin.platform.domain.enums.rank.PointAction;
import com.tontin.platform.domain.enums.round.RoundStatus;
import com.tontin.platform.dto.payment.request.CreatePaymentIntentRequest;
import com.tontin.platform.dto.payment.response.CanPayResponse;
import com.tontin.platform.dto.payment.response.CreatePaymentIntentResponse;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.repository.PaymentRepository;
import com.tontin.platform.repository.RoundRepository;
import com.tontin.platform.service.NotificationService;
import com.tontin.platform.service.PaymentService;
import com.tontin.platform.service.PointsService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook-secret:}")
    private String stripeWebhookSecret;

    private final SecurityUtils securityUtils;
    private final MemberRepository memberRepository;
    private final DartRepository dartRepository;
    private final RoundRepository roundRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final PointsService pointsService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String getStripeSecretKey() {
        return stripeSecretKey != null ? stripeSecretKey.trim() : "";
    }

    /**
     * Core idempotent logic: marks a payment as PAYED and closes the round
     * when all contributing members have paid.
     *
     * This is shared by the webhook path (markPaymentSucceeded) and the
     * synchronous client-side confirmation path (confirmPaymentById).
     */
    private void applyPaymentSuccess(Payment payment) {
        if (payment.getPaymentStatus() == PaymentStatus.PAYED) {
            log.debug(
                "Payment {} is already PAYED — skipping.",
                payment.getId()
            );
            return;
        }

        payment.setPaymentStatus(PaymentStatus.PAYED);
        payment.setDate(LocalDateTime.now());
        paymentRepository.save(payment);
        log.info("Payment {} marked as PAYED.", payment.getId());

        // Notify the payer that their payment was received
        Member payer = payment.getPayer();
        if (payer != null && payer.getUser() != null) {
            Round r = payment.getRound();
            Dart d = r != null ? r.getDart() : null;
            String dartName = d != null && d.getName() != null ? d.getName() : "the Dâr";
            notificationService.create(
                payer.getUser().getId(),
                NotificationType.PAYMENT_RECEIVED,
                "Payment received",
                String.format("Your contribution for \"%s\" has been received.", dartName),
                d != null ? "/dashboard/client/dar/" + d.getId() : null,
                "View Dâr"
            );
            if (d != null) {
                pointsService.addPoints(payer.getUser().getId(), PointAction.PAYMENT_ON_TIME, d.getId());
            }
        }

        // Check if all non-recipient members of this round have now paid.
        // Use DISTINCT payer count so a member who paid twice is only counted once.
        Round round = payment.getRound();
        Dart dart = round.getDart();
        int activeCount = dart.getActiveMembers().size();
        long paidCount =
            paymentRepository.countDistinctPayersByRoundIdAndStatus(
                round.getId(),
                PaymentStatus.PAYED
            );

        // All payers = active members minus the one recipient
        if (paidCount >= activeCount - 1) {
            round.setStatus(RoundStatus.PAYED);
            roundRepository.save(round);
            log.info(
                "Round {} marked as PAYED — all {} contributions received.",
                round.getId(),
                paidCount
            );
            // Notify the round recipient that their payout is ready
            Member recipient = round.getRecipient();
            if (recipient != null && recipient.getUser() != null) {
                UUID recipientUserId = recipient.getUser().getId();
                String dartName = dart.getName() != null ? dart.getName() : "Your Dâr";
                String title = "Payout ready";
                String description = String.format(
                    "All contributions for this round of \"%s\" have been received. Your payout is ready.",
                    dartName
                );
                String actionUrl = "/dashboard/client/dar/" + dart.getId();
                notificationService.create(
                    recipientUserId,
                    NotificationType.PAYOUT_READY,
                    title,
                    description,
                    actionUrl,
                    "View Dâr"
                );
                pointsService.addPoints(recipientUserId, PointAction.PAYOUT_RECEIVED, dart.getId());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Service interface implementations
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public CanPayResponse canPay(UUID dartId) {
        UUID userId = securityUtils.requireCurrentUserId();

        Member payer = memberRepository
            .findByDartIdAndUserId(dartId, userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this Dâr."
                )
            );

        Dart dart = dartRepository
            .findById(dartId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This Dâr was not found."
                )
            );

        List<Round> current = roundRepository
            .findCurrentRoundByDartId(
                dartId,
                RoundStatus.INPAYED,
                PageRequest.of(0, 1)
            )
            .getContent();

        if (current.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "There is no contribution due for this Dâr right now. All rounds may be completed."
            );
        }

        Round round = current.get(0);

        if (round.getDate() != null) {
            LocalDateTime paymentOpenDate = round.getDate().minusDays(5);
            if (LocalDateTime.now().isBefore(paymentOpenDate)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payments open 5 days before each round. You can pay from " +
                        paymentOpenDate.toLocalDate() + "."
                );
            }
        }

        if (
            round.getRecipient() != null &&
            round.getRecipient().getId().equals(payer.getId())
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You are receiving this round — no payment needed from you."
            );
        }

        return CanPayResponse.builder().canPay(true).build();
    }

    @Override
    @Transactional
    public CreatePaymentIntentResponse createPaymentIntent(
        CreatePaymentIntentRequest request
    ) {
        UUID dartId = request.dartId();
        UUID userId = securityUtils.requireCurrentUserId();

        Member payer = memberRepository
            .findByDartIdAndUserId(dartId, userId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You are not a member of this Dâr."
                )
            );

        Dart dart = dartRepository
            .findById(dartId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "This Dâr was not found."
                )
            );

        List<Round> current = roundRepository
            .findCurrentRoundByDartId(
                dartId,
                RoundStatus.INPAYED,
                PageRequest.of(0, 1)
            )
            .getContent();

        if (current.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "There is no contribution due for this Dâr right now. All rounds may be completed."
            );
        }

        Round round = current.get(0);

        // Enforce payment window: members can only pay starting 5 days before the round date
        if (round.getDate() != null) {
            LocalDateTime paymentOpenDate = round.getDate().minusDays(5);
            if (LocalDateTime.now().isBefore(paymentOpenDate)) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Payments open 5 days before each round. You can pay from " +
                        paymentOpenDate.toLocalDate() + "."
                );
            }
        }

        if (
            round.getRecipient() != null &&
            round.getRecipient().getId().equals(payer.getId())
        ) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You are receiving this round — no payment needed from you."
            );
        }

        // Block paying again in the same round
        boolean alreadyPaid = paymentRepository
            .findAllByRoundIdAndPaymentStatus(round.getId(), PaymentStatus.PAYED)
            .stream()
            .anyMatch(p -> p.getPayer().getId().equals(payer.getId()));
        if (alreadyPaid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You have already paid for this round. You cannot pay again for the same tour."
            );
        }

        BigDecimal amount = dart.getMonthlyContribution();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid contribution amount."
            );
        }

        long amountCents = amount.multiply(BigDecimal.valueOf(100)).longValue();
        if (amountCents < 50) {
            amountCents = 50; // Stripe minimum
        }

        // Idempotent: reuse an existing PENDING payment for this payer/round
        Payment payment = null;
        List<Payment> existing =
            paymentRepository.findAllByRoundIdAndPaymentStatus(
                round.getId(),
                PaymentStatus.PENDING
            );

        for (Payment p : existing) {
            if (!p.getPayer().getId().equals(payer.getId())) continue;

            String existingIntentId = p.getStripePaymentIntentId();
            // Try to reuse a real (non-mock) Stripe intent
            if (
                existingIntentId != null &&
                !existingIntentId.startsWith("pi_mock_")
            ) {
                try {
                    PaymentIntent intent = PaymentIntent.retrieve(
                        existingIntentId
                    );
                    if (
                        "requires_payment_method".equals(intent.getStatus()) ||
                        "requires_confirmation".equals(intent.getStatus())
                    ) {
                        return CreatePaymentIntentResponse.builder()
                            .clientSecret(intent.getClientSecret())
                            .paymentId(p.getId())
                            .build();
                    }
                } catch (Exception e) {
                    log.warn(
                        "Could not retrieve existing intent, will create new one: {}",
                        e.getMessage()
                    );
                }
            }

            // Reuse this payment record — create a fresh Stripe intent below
            payment = p;
            break;
        }

        if (payment == null) {
            payment = Payment.builder()
                .amount(amount)
                .method("STRIPE")
                .date(LocalDateTime.now())
                .paymentStatus(PaymentStatus.PENDING)
                .round(round)
                .payer(payer)
                .build();
            payment = paymentRepository.save(payment);
        }

        String secretKey = getStripeSecretKey();
        boolean isRealKey =
            secretKey.length() > 0 &&
            (secretKey.startsWith("sk_") || secretKey.startsWith("rk_"));

        if (isRealKey) {
            try {
                Stripe.apiKey = secretKey;
                PaymentIntent intent = PaymentIntent.create(
                    new com.stripe.param.PaymentIntentCreateParams.Builder()
                        .setAmount(amountCents)
                        .setCurrency("usd")
                        .putMetadata("paymentId", payment.getId().toString())
                        .putMetadata("roundId", round.getId().toString())
                        .putMetadata("dartId", dartId.toString())
                        .setAutomaticPaymentMethods(
                            com.stripe.param.PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build()
                        )
                        .build()
                );
                payment.setStripePaymentIntentId(intent.getId());
                paymentRepository.save(payment);
                return CreatePaymentIntentResponse.builder()
                    .clientSecret(intent.getClientSecret())
                    .paymentId(payment.getId())
                    .build();
            } catch (Exception e) {
                log.error(
                    "Stripe PaymentIntent create failed: {}",
                    e.getMessage(),
                    e
                );
                String msg =
                    e.getMessage() != null &&
                    e.getMessage().contains("Invalid API Key")
                        ? "Stripe secret key is invalid. Check STRIPE_SECRET_KEY in .env."
                        : "Could not create payment session. Try again.";
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    msg
                );
            }
        }

        // Mock key — return a fake client secret so the frontend can render the form
        String mockSecret = "pi_mock_" + payment.getId() + "_secret_mock";
        payment.setStripePaymentIntentId("pi_mock_" + payment.getId());
        paymentRepository.save(payment);
        return CreatePaymentIntentResponse.builder()
            .clientSecret(mockSecret)
            .paymentId(payment.getId())
            .build();
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {
        String webhookSecret =
            stripeWebhookSecret != null ? stripeWebhookSecret.trim() : "";

        if (webhookSecret.isBlank() || webhookSecret.startsWith("whsec_mock")) {
            log.warn(
                "Stripe webhook secret not configured; skipping verification."
            );
            return;
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, signature, webhookSecret);
        } catch (SignatureVerificationException e) {
            log.warn(
                "Stripe webhook signature verification failed: {}",
                e.getMessage()
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid signature"
            );
        }

        if ("payment_intent.succeeded".equals(event.getType())) {
            PaymentIntent intent = (PaymentIntent) event
                .getDataObjectDeserializer()
                .getObject()
                .orElse(null);
            if (intent != null) {
                markPaymentSucceeded(intent.getId());
            }
        }
    }

    @Override
    @Transactional
    public void markPaymentSucceeded(String stripePaymentIntentId) {
        paymentRepository
            .findByStripePaymentIntentId(stripePaymentIntentId)
            .ifPresentOrElse(this::applyPaymentSuccess, () ->
                log.warn(
                    "No payment found for Stripe intent id: {}",
                    stripePaymentIntentId
                )
            );
    }

    @Override
    @Transactional
    public void confirmPaymentById(UUID paymentId) {
        UUID currentUserId = securityUtils.requireCurrentUserId();

        Payment payment = paymentRepository
            .findById(paymentId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment not found."
                )
            );

        // Security: only the payer themselves can confirm their own payment
        if (
            payment.getPayer() == null ||
            payment.getPayer().getUser() == null ||
            !payment.getPayer().getUser().getId().equals(currentUserId)
        ) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You are not authorised to confirm this payment."
            );
        }

        applyPaymentSuccess(payment);
    }

    @Override
    public List<UUID> getPaidPayerMemberIdsForRound(UUID roundId) {
        return paymentRepository
            .findAllByRoundIdAndPaymentStatus(roundId, PaymentStatus.PAYED)
            .stream()
            .map(p -> p.getPayer().getId())
            .distinct()
            .toList();
    }
}