package com.tontin.platform.repository;

import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository
    extends JpaRepository<Payment, UUID>, JpaSpecificationExecutor<Payment> {
    List<Payment> findAllByRoundIdOrderByDateAsc(UUID roundId);

    List<Payment> findAllByPayerIdOrderByDateDesc(UUID payerMemberId);

    @Query(
        "SELECT p FROM Payment p WHERE p.round.dart.id = :dartId ORDER BY p.date DESC"
    )
    List<Payment> findAllByDartId(@Param("dartId") UUID dartId);

    List<Payment> findAllByRoundIdAndPaymentStatus(
        UUID roundId,
        PaymentStatus status
    );

    boolean existsByRoundIdAndPayerIdAndPaymentStatus(
        UUID roundId,
        UUID payerId,
        PaymentStatus status
    );

    long countByRoundIdAndPaymentStatus(UUID roundId, PaymentStatus status);

    java.util.Optional<Payment> findByStripePaymentIntentId(
        String stripePaymentIntentId
    );

    /**
     * Fetch all payments for an entire dart that have a given status.
     * Used for batch-loading paid member IDs across all rounds of a dart
     * without N+1 queries.
     */
    @Query(
        "SELECT p FROM Payment p WHERE p.round.dart.id = :dartId AND p.paymentStatus = :status"
    )
    List<Payment> findAllByDartIdAndPaymentStatus(
        @Param("dartId") UUID dartId,
        @Param("status") PaymentStatus status
    );

    /**
     * Fetch all payments for a single round that have a given status.
     * Convenience alias kept consistent with the dart-level variant.
     */
    @Query(
        "SELECT p FROM Payment p WHERE p.round.id = :roundId AND p.paymentStatus = :status"
    )
    List<Payment> findAllByRoundIdAndStatus(
        @Param("roundId") UUID roundId,
        @Param("status") PaymentStatus status
    );

    /**
     * Count distinct payers (by member id) who have a payment with the given
     * status for a round. Used instead of countByRoundIdAndPaymentStatus so
     * that a member who paid twice for the same round is only counted once
     * when deciding whether the round should be closed.
     */
    @Query(
        "SELECT COUNT(DISTINCT p.payer.id) FROM Payment p WHERE p.round.id = :roundId AND p.paymentStatus = :status"
    )
    long countDistinctPayersByRoundIdAndStatus(
        @Param("roundId") UUID roundId,
        @Param("status") PaymentStatus status
    );

    @Query(
        """
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        JOIN p.payer payer JOIN payer.user u
        WHERE u.id = :userId AND p.paymentStatus = :status
        AND p.date >= :from AND p.date < :to
        """
    )
    BigDecimal sumAmountByUserStatusAndDateRange(
        @Param("userId") UUID userId,
        @Param("status") PaymentStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
        """
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        JOIN p.payer payer JOIN payer.user u
        JOIN p.round r JOIN r.dart d
        WHERE u.id = :userId AND d.id = :dartId AND p.paymentStatus = :status
        AND p.date >= :from AND p.date < :to
        """
    )
    BigDecimal sumAmountByUserDartStatusAndDateRange(
        @Param("userId") UUID userId,
        @Param("dartId") UUID dartId,
        @Param("status") PaymentStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
        """
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        WHERE p.paymentStatus = :status
        AND p.date >= :from AND p.date < :to
        """
    )
    BigDecimal sumAmountByStatusAndDateRange(
        @Param("status") PaymentStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
        """
        SELECT COALESCE(SUM(p.amount), 0) FROM Payment p
        JOIN p.round r JOIN r.dart d
        WHERE d.id = :dartId AND p.paymentStatus = :status
        AND p.date >= :from AND p.date < :to
        """
    )
    BigDecimal sumAmountByDartStatusAndDateRange(
        @Param("dartId") UUID dartId,
        @Param("status") PaymentStatus status,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    /**
     * Monthly totals of PAYED contributions for charts (PostgreSQL).
     */
    @Query(
        value =
            """
            SELECT date_trunc('month', p.payment_date) AS bucket,
                   COALESCE(SUM(p.amount), 0) AS total
            FROM payments p
            INNER JOIN members m ON p.payer_member_id = m.id
            INNER JOIN rounds r ON p.round_id = r.id
            WHERE m.user_id = CAST(:userId AS uuid)
            AND p.payment_status = 'PAYED'
            AND p.payment_date >= :from AND p.payment_date < :to
            GROUP BY date_trunc('month', p.payment_date)
            ORDER BY bucket
            """,
        nativeQuery = true
    )
    List<Object[]> aggregateMonthlyPayedContributionsAllDarts(
        @Param("userId") UUID userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to
    );

    @Query(
        value =
            """
            SELECT date_trunc('month', p.payment_date) AS bucket,
                   COALESCE(SUM(p.amount), 0) AS total
            FROM payments p
            INNER JOIN members m ON p.payer_member_id = m.id
            INNER JOIN rounds r ON p.round_id = r.id
            WHERE m.user_id = CAST(:userId AS uuid)
            AND p.payment_status = 'PAYED'
            AND p.payment_date >= :from AND p.payment_date < :to
            AND r.dart_id = CAST(:dartId AS uuid)
            GROUP BY date_trunc('month', p.payment_date)
            ORDER BY bucket
            """,
        nativeQuery = true
    )
    List<Object[]> aggregateMonthlyPayedContributionsForDart(
        @Param("userId") UUID userId,
        @Param("from") LocalDateTime from,
        @Param("to") LocalDateTime to,
        @Param("dartId") UUID dartId
    );
}
