package com.tontin.platform.domain.enums.logs;

/**
 * Represents the severity levels for application logging.
 *
 * <p>These levels follow standard logging conventions and are ordered
 * from most detailed (TRACE) to most severe (ERROR).</p>
 *
 * <ul>
 *   <li><b>TRACE</b>: Very detailed information, typically of interest only when diagnosing problems.</li>
 *   <li><b>DEBUG</b>: Detailed information useful for debugging during development.</li>
 *   <li><b>INFO</b>: Informational messages that highlight the progress of the application.</li>
 *   <li><b>WARN</b>: Warning messages indicating potentially harmful situations.</li>
 *   <li><b>ERROR</b>: Error events that might still allow the application to continue running.</li>
 * </ul>
 */
public enum LogsLevel {
    /**
     * Very detailed diagnostic information
     */
    TRACE,

    /**
     * Debug-level messages for development
     */
    DEBUG,

    /**
     * Informational messages about application progress
     */
    INFO,

    /**
     * Warning messages for potentially harmful situations
     */
    WARN,

    /**
     * Error messages for error events
     */
    ERROR,
}
