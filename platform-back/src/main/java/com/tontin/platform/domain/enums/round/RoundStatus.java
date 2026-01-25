package com.tontin.platform.domain.enums.round;

/**
 * Represents the payment status of a tontine round.
 *
 * <ul>
 *   <li>{@link #PAYED} – the round has been fully paid.</li>
 *   <li>{@link #INPAYED} – the round is pending payment or only partially processed.</li>
 * </ul>
 */
public enum RoundStatus {

    /**
     * The round has been fully settled.
     */
    PAYED,

    /**
     * The round is still awaiting payment or processing.
     */
    INPAYED
}
