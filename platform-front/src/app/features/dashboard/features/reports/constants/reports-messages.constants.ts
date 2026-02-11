/**
 * Error and success message constants for reports feature
 *
 * This file contains all user-facing messages for consistency.
 */

/**
 * Error messages
 */
export const REPORTS_ERROR_MESSAGES = {
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  LOAD_FAILED: 'Failed to load data. Please try again.',
} as const;

/**
 * Success messages
 */
export const REPORTS_SUCCESS_MESSAGES = {
  ACTION_SUCCESS: 'Action completed successfully!',
  CHANGES_SAVED: 'Changes saved successfully!',
} as const;

/**
 * Confirmation messages
 */
export const REPORTS_CONFIRMATION_MESSAGES = {
  CONFIRM_ACTION: 'Are you sure you want to perform this action?',
} as const;

/**
 * Info messages
 */
export const REPORTS_INFO_MESSAGES = {
  NO_DATA: 'No data available.',
} as const;

/**
 * Validation messages
 */
export const REPORTS_VALIDATION_MESSAGES = {
  REQUIRED_FIELD: 'This field is required',
} as const;

/**
 * Loading messages
 */
export const REPORTS_LOADING_MESSAGES = {
  LOADING: 'Loading...',
} as const;

/**
 * Export all message constants
 */
export const REPORTS_MESSAGES = {
  ERROR: REPORTS_ERROR_MESSAGES,
  SUCCESS: REPORTS_SUCCESS_MESSAGES,
  CONFIRMATION: REPORTS_CONFIRMATION_MESSAGES,
  INFO: REPORTS_INFO_MESSAGES,
  VALIDATION: REPORTS_VALIDATION_MESSAGES,
  LOADING: REPORTS_LOADING_MESSAGES,
} as const;
