package com.tontin.platform.util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for handling payment frequency calculations.
 */
public class PaymentFrequencyUtil {

    /**
     * Calculate the number of days between payments based on frequency.
     *
     * @param paymentFrequency the payment frequency string (WEEKLY, BI-WEEKLY, MONTH, QUARTERLY)
     * @return number of days between payments
     */
    public static int getDaysBetweenPayments(String paymentFrequency) {
        if (paymentFrequency == null) {
            throw new IllegalArgumentException("Payment frequency cannot be null");
        }

        return switch (paymentFrequency.toUpperCase()) {
            case "WEEKLY" -> 7;
            case "BI-WEEKLY", "BIWEEKLY" -> 14;
            case "MONTH", "MONTHLY" -> 30;
            case "QUARTERLY" -> 90;
            default -> throw new IllegalArgumentException(
                "Unknown payment frequency: " + paymentFrequency
            );
        };
    }

    /**
     * Calculate the round payment date based on frequency.
     * Round 1 = first payment date (e.g. 1 month after start if MONTH),
     * Round 2 = second payment date, etc.
     * Example: start 1/1/2026, MONTH → Round 1 = 1/2/2026, Round 2 = 1/3/2026.
     *
     * @param startDate the dart start date
     * @param paymentFrequency the payment frequency string (WEEKLY, BI-WEEKLY, MONTH, QUARTERLY)
     * @param roundNumber the round number (1-based)
     * @return the date when this round's payment is due / recipient gets the money
     */
    public static LocalDateTime calculateRoundDate(
        LocalDateTime startDate,
        String paymentFrequency,
        int roundNumber
    ) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (roundNumber < 1) {
            throw new IllegalArgumentException("Round number must be >= 1");
        }

        String upper = paymentFrequency != null ? paymentFrequency.toUpperCase() : "";
        // Use months for MONTH/MONTHLY and days for others
        if ("MONTH".equals(upper) || "MONTHLY".equals(upper)) {
            return startDate.plusMonths(roundNumber);
        }
        if ("QUARTERLY".equals(upper)) {
            return startDate.plusMonths(roundNumber * 3L);
        }
        int daysBetweenPayments = getDaysBetweenPayments(paymentFrequency);
        // Round 1 = first payment (1 period after start), round 2 = 2 periods, etc.
        int daysToAdd = roundNumber * daysBetweenPayments;
        return startDate.plusDays(daysToAdd);
    }

    /**
     * Calculate the number of days to add for a specific round number.
     * Round 1 = 1 period after start, round 2 = 2 periods, etc.
     *
     * @param paymentFrequency the payment frequency string
     * @param roundNumber the round number (1-based)
     * @return number of days to add from start date (not used for MONTH/QUARTERLY)
     */
    public static long getDaysToAdd(String paymentFrequency, int roundNumber) {
        int daysBetweenPayments = getDaysBetweenPayments(paymentFrequency);
        return (long) roundNumber * daysBetweenPayments;
    }

    /**
     * Validate payment frequency string.
     *
     * @param paymentFrequency the payment frequency to validate
     * @return true if valid
     */
    public static boolean isValidPaymentFrequency(String paymentFrequency) {
        if (paymentFrequency == null) {
            return false;
        }
        String upper = paymentFrequency.toUpperCase();
        return upper.equals("WEEKLY") ||
            upper.equals("BI-WEEKLY") ||
            upper.equals("BIWEEKLY") ||
            upper.equals("MONTH") ||
            upper.equals("MONTHLY") ||
            upper.equals("QUARTERLY");
    }
}
