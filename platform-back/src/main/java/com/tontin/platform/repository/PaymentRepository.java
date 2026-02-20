package com.tontin.platform.repository;

import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findAllByRoundIdOrderByDateAsc(UUID roundId);

    List<Payment> findAllByPayerIdOrderByDateDesc(UUID payerMemberId);

    @Query("SELECT p FROM Payment p WHERE p.round.dart.id = :dartId ORDER BY p.date DESC")
    List<Payment> findAllByDartId(@Param("dartId") UUID dartId);

    List<Payment> findAllByRoundIdAndPaymentStatus(UUID roundId, PaymentStatus status);

    long countByRoundIdAndPaymentStatus(UUID roundId, PaymentStatus status);

    java.util.Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);
}
