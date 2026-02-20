package com.tontin.platform.domain.enums.payment;

/**
 * Status of a contribution payment.
 *
 * <ul>
 *   <li>{@link #PENDING} – payment not yet completed.</li>
 *   <li>{@link #PAYED} – payment completed.</li>
 *   <li>{@link #CANCELLED} – payment was cancelled or annulled.</li>
 * </ul>
 */
public enum PaymentStatus {

    PENDING,

    PAYED,

    CANCELLED
}
