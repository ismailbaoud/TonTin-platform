package com.tontin.platform.repository.spec;

import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

/**
 * Dynamic filters for the client payment report (my payments).
 */
public final class PaymentSpecifications {

    private PaymentSpecifications() {}

    public static Specification<Payment> forUser(UUID userId) {
        return (root, query, cb) ->
            userId == null
                ? cb.conjunction()
                : cb.equal(
                    root.join("payer", JoinType.INNER).join("user", JoinType.INNER).get("id"),
                    userId
                );
    }

    public static Specification<Payment> forDartId(UUID dartId) {
        return (root, query, cb) ->
            dartId == null
                ? cb.conjunction()
                : cb.equal(
                    root.join("round", JoinType.INNER).join("dart", JoinType.INNER).get("id"),
                    dartId
                );
    }

    public static Specification<Payment> betweenInclusiveStartExclusiveEnd(
        LocalDateTime from,
        LocalDateTime to
    ) {
        return (root, query, cb) ->
            cb.and(
                cb.greaterThanOrEqualTo(root.get("date"), from),
                cb.lessThan(root.get("date"), to)
            );
    }

    public static Specification<Payment> hasStatus(PaymentStatus status) {
        return (root, query, cb) ->
            status == null ? cb.conjunction() : cb.equal(root.get("paymentStatus"), status);
    }
}
