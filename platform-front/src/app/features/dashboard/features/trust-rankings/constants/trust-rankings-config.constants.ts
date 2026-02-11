/**
 * Configuration constants for trust-rankings feature
 *
 * This file contains all the configuration values, magic numbers,
 * and constant values used throughout the trust-rankings feature.
 */

/**
 * Pagination settings
 */
export const TRUST-RANKINGS_PAGINATION = {
  /** Default page size */
  DEFAULT_PAGE_SIZE: 20,

  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
} as const;

/**
 * Timing constants
 */
export const TRUST-RANKINGS_TIMING = {
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
export const TRUST-RANKINGS_FEATURES = {
  /** Enable/disable specific features */
  ENABLE_FEATURE_X: true,
} as const;

/**
 * Export all constants as a single object
 */
export const TRUST-RANKINGS_CONFIG = {
  PAGINATION: TRUST-RANKINGS_PAGINATION,
  TIMING: TRUST-RANKINGS_TIMING,
  FEATURES: TRUST-RANKINGS_FEATURES,
} as const;
