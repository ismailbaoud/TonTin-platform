/**
 * Configuration constants for Dâr feature
 *
 * This file contains all the configuration values, magic numbers,
 * and constant values used throughout the Dâr feature.
 */

/**
 * Default pagination settings for Dâr lists
 */
export const DAR_PAGINATION = {
  /** Default page size for Dâr lists */
  DEFAULT_PAGE_SIZE: 20,

  /** Default page size for transactions */
  TRANSACTIONS_PAGE_SIZE: 20,

  /** Default page size for messages */
  MESSAGES_PAGE_SIZE: 50,

  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
} as const;

/**
 * Validation rules and limits for Dâr creation
 */
export const DAR_LIMITS = {
  /** Minimum number of members required to start a Dâr */
  MIN_MEMBERS: 2,

  /** Maximum number of members allowed in a Dâr */
  MAX_MEMBERS: 50,

  /** Minimum contribution amount (in base currency units) */
  MIN_CONTRIBUTION: 1000,

  /** Maximum contribution amount (in base currency units) */
  MAX_CONTRIBUTION: 10000000,

  /** Minimum name length */
  MIN_NAME_LENGTH: 3,

  /** Maximum name length */
  MAX_NAME_LENGTH: 100,

  /** Maximum description length */
  MAX_DESCRIPTION_LENGTH: 500,

  /** Maximum rules length */
  MAX_RULES_LENGTH: 2000,

  /** Maximum message content length */
  MAX_MESSAGE_LENGTH: 1000,
} as const;

/**
 * Time-related constants for Dâr operations
 */
export const DAR_TIMING = {
  /** Polling interval for Dâr details updates (in milliseconds) */
  DETAILS_POLL_INTERVAL: 30000, // 30 seconds

  /** Polling interval for messages (in milliseconds) */
  MESSAGES_POLL_INTERVAL: 5000, // 5 seconds

  /** Debounce time for search input (in milliseconds) */
  SEARCH_DEBOUNCE_TIME: 300,

  /** Auto-refresh interval for Dâr list (in milliseconds) */
  LIST_REFRESH_INTERVAL: 60000, // 1 minute

  /** Toast notification duration (in milliseconds) */
  TOAST_DURATION: 5000,
} as const;

/**
 * Default values for Dâr creation
 */
export const DAR_DEFAULTS = {
  /** Default contribution amount */
  DEFAULT_CONTRIBUTION: 10000,

  /** Default maximum members */
  DEFAULT_MAX_MEMBERS: 10,

  /** Default frequency */
  DEFAULT_FREQUENCY: 'monthly' as const,

  /** Default visibility */
  DEFAULT_VISIBILITY: 'private' as const,
} as const;

/**
 * Feature flags for Dâr functionality
 */
export const DAR_FEATURES = {
  /** Enable/disable public Dâr discovery */
  ENABLE_PUBLIC_DISCOVERY: true,

  /** Enable/disable Dâr chat/messaging */
  ENABLE_MESSAGING: true,

  /** Enable/disable trust score display */
  ENABLE_TRUST_SCORES: true,

  /** Enable/disable member reporting */
  ENABLE_REPORTING: true,

  /** Enable/disable Dâr statistics for organizers */
  ENABLE_STATISTICS: true,
} as const;

/**
 * UI-related constants
 */
export const DAR_UI = {
  /** Number of Dârs to show in "quick view" sections */
  QUICK_VIEW_LIMIT: 5,

  /** Number of recent transactions to show by default */
  RECENT_TRANSACTIONS_LIMIT: 10,

  /** Number of recent messages to show by default */
  RECENT_MESSAGES_LIMIT: 20,

  /** Skeleton loader count for lists */
  SKELETON_COUNT: 6,
} as const;

/**
 * Trust score thresholds
 */
export const TRUST_SCORE_THRESHOLDS = {
  /** Minimum trust score to join most Dârs */
  MIN_RECOMMENDED: 60,

  /** Trust score considered "good" */
  GOOD_SCORE: 75,

  /** Trust score considered "excellent" */
  EXCELLENT_SCORE: 90,

  /** Warning threshold - below this shows warning */
  WARNING_THRESHOLD: 50,
} as const;

/**
 * Payment-related constants
 */
export const DAR_PAYMENT = {
  /** Grace period for late payments (in days) */
  LATE_PAYMENT_GRACE_DAYS: 3,

  /** Number of days before payment due to send reminder */
  REMINDER_DAYS_BEFORE: 2,

  /** Penalty percentage for late payments (as decimal) */
  LATE_PAYMENT_PENALTY: 0.05, // 5%
} as const;

/**
 * Export all constants as a single object for convenience
 */
export const DAR_CONFIG = {
  PAGINATION: DAR_PAGINATION,
  LIMITS: DAR_LIMITS,
  TIMING: DAR_TIMING,
  DEFAULTS: DAR_DEFAULTS,
  FEATURES: DAR_FEATURES,
  UI: DAR_UI,
  TRUST_SCORE: TRUST_SCORE_THRESHOLDS,
  PAYMENT: DAR_PAYMENT,
} as const;
