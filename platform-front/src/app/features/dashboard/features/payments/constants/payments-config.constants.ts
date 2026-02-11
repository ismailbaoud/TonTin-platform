/**
 * Configuration constants for payments feature
 *
 * This file contains all the configuration values, magic numbers,
 * and constant values used throughout the payments feature.
 */

/**
 * Pagination settings
 */
export const PAYMENTS_PAGINATION = {
  /** Default page size */
  DEFAULT_PAGE_SIZE: 20,

  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
} as const;

/**
 * Timing constants
 */
export const PAYMENTS_TIMING = {
  /** Polling interval (in milliseconds) */
  POLL_INTERVAL: 30000, // 30 seconds

  /** Debounce time for search (in milliseconds) */
  SEARCH_DEBOUNCE_TIME: 300,

  /** Toast notification duration (in milliseconds) */
  TOAST_DURATION: 5000,
} as const;

/**
 * Feature flags
 */
export const PAYMENTS_FEATURES = {
  /** Enable/disable specific features */
  ENABLE_FEATURE_X: true,
} as const;

/**
 * Export all constants as a single object
 */
export const PAYMENTS_CONFIG = {
  PAGINATION: PAYMENTS_PAGINATION,
  TIMING: PAYMENTS_TIMING,
  FEATURES: PAYMENTS_FEATURES,
} as const;
