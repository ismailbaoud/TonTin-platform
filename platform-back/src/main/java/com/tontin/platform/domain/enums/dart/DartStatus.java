package com.tontin.platform.domain.enums.dart;

/**
 * Represents the lifecycle status of a dart (tontine/savings circle).
 *
 * <ul>
 *   <li><b>PENDING</b>: Dart has been created but not yet started. Waiting for sufficient members or start date.</li>
 *   <li><b>ACTIVE</b>: Dart is currently running with active contributions and allocations.</li>
 *   <li><b>FINISHED</b>: Dart has completed its cycle and all distributions have been made.</li>
 * </ul>
 */
public enum DartStatus {
    /**
     * Dart has been created but not yet activated
     */
    PENDING,

    /**
     * Dart is currently active and running
     */
    ACTIVE,

    /**
     * Dart has completed its cycle
     */
    FINISHED,
}
