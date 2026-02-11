/**
 * Error and success message constants for payments feature
 *
 * This file contains all user-facing messages for consistency.
 */

/**
 * Error messages
 */
export const PAYMENTS_ERROR_MESSAGES = {
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  LOAD_FAILED: 'Failed to load data. Please try again.',
} as const;

/**
 * Success messages
 */
export const PAYMENTS_SUCCESS_MESSAGES = {
  ACTION_SUCCESS: 'Action completed successfully!',
  CHANGES_SAVED: 'Changes saved successfully!',
} as const;

/**
 * Confirmation messages
 */
export const PAYMENTS_CONFIRMATION_MESSAGES = {
  CONFIRM_ACTION: 'Are you sure you want to perform this action?',
} as const;

/**
 * Info messages
 */
export const PAYMENTS_INFO_MESSAGES = {
  NO_DATA: 'No data available.',
} as const;

/**
 * Validation messages
 */
export const PAYMENTS_VALIDATION_MESSAGES = {
  REQUIRED_FIELD: 'This field is required',
} as const;

/**
 * Loading messages
 */
export const PAYMENTS_LOADING_MESSAGES = {
  LOADING: 'Loading...',
} as const;

/**
 * Export all message constants
 */
export const PAYMENTS_MESSAGES = {
  ERROR: PAYMENTS_ERROR_MESSAGES,
  SUCCESS: PAYMENTS_SUCCESS_MESSAGES,
  CONFIRMATION: PAYMENTS_CONFIRMATION_MESSAGES,
  INFO: PAYMENTS_INFO_MESSAGES,
  VALIDATION: PAYMENTS_VALIDATION_MESSAGES,
  LOADING: PAYMENTS_LOADING_MESSAGES,
} as const;
