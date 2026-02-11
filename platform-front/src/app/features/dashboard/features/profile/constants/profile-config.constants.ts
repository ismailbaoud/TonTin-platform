/**
 * Configuration constants for profile feature
 *
 * This file contains all the configuration values, magic numbers,
 * and constant values used throughout the profile feature.
 */

/**
 * Pagination settings
 */
export const PROFILE_PAGINATION = {
  /** Default page size */
  DEFAULT_PAGE_SIZE: 20,

  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
} as const;

/**
 * Timing constants
 */
export const PROFILE_TIMING = {
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
export const PROFILE_FEATURES = {
  /** Enable/disable specific features */
  ENABLE_FEATURE_X: true,
} as const;

/**
 * Export all constants as a single object
 */
export const PROFILE_CONFIG = {
  PAGINATION: PROFILE_PAGINATION,
  TIMING: PROFILE_TIMING,
  FEATURES: PROFILE_FEATURES,
} as const;
