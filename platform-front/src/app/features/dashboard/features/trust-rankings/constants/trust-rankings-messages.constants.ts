/**
 * Error and success message constants for trust-rankings feature
 *
 * This file contains all user-facing messages for consistency.
 */

/**
 * Error messages
 */
export const TRUST-RANKINGS_ERROR_MESSAGES = {
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  LOAD_FAILED: 'Failed to load data. Please try again.',
} as const;

/**
 * Success messages
 */
export const TRUST-RANKINGS_SUCCESS_MESSAGES = {
  ACTION_SUCCESS: 'Action completed successfully!',
  CHANGES_SAVED: 'Changes saved successfully!',
} as const;

/**
 * Confirmation messages
 */
export const TRUST-RANKINGS_CONFIRMATION_MESSAGES = {
  CONFIRM_ACTION: 'Are you sure you want to perform this action?',
} as const;

/**
 * Info messages
 */
export const TRUST-RANKINGS_INFO_MESSAGES = {
  NO_DATA: 'No data available.',
} as const;

/**
 * Validation messages
 */
export const TRUST-RANKINGS_VALIDATION_MESSAGES = {
  REQUIRED_FIELD: 'This field is required',
} as const;

/**
 * Loading messages
 */
export const TRUST-RANKINGS_LOADING_MESSAGES = {
  LOADING: 'Loading...',
} as const;

/**
 * Export all message constants
 */
export const TRUST-RANKINGS_MESSAGES = {
  ERROR: TRUST-RANKINGS_ERROR_MESSAGES,
  SUCCESS: TRUST-RANKINGS_SUCCESS_MESSAGES,
  CONFIRMATION: TRUST-RANKINGS_CONFIRMATION_MESSAGES,
  INFO: TRUST-RANKINGS_INFO_MESSAGES,
  VALIDATION: TRUST-RANKINGS_VALIDATION_MESSAGES,
  LOADING: TRUST-RANKINGS_LOADING_MESSAGES,
} as const;
