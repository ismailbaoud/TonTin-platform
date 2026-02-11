/**
 * Error and success message constants for admin feature
 *
 * This file contains all user-facing messages for consistency.
 */

/**
 * Error messages
 */
export const ADMIN_ERROR_MESSAGES = {
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  LOAD_FAILED: 'Failed to load data. Please try again.',
} as const;

/**
 * Success messages
 */
export const ADMIN_SUCCESS_MESSAGES = {
  ACTION_SUCCESS: 'Action completed successfully!',
  CHANGES_SAVED: 'Changes saved successfully!',
} as const;

/**
 * Confirmation messages
 */
export const ADMIN_CONFIRMATION_MESSAGES = {
  CONFIRM_ACTION: 'Are you sure you want to perform this action?',
} as const;

/**
 * Info messages
 */
export const ADMIN_INFO_MESSAGES = {
  NO_DATA: 'No data available.',
} as const;

/**
 * Validation messages
 */
export const ADMIN_VALIDATION_MESSAGES = {
  REQUIRED_FIELD: 'This field is required',
} as const;

/**
 * Loading messages
 */
export const ADMIN_LOADING_MESSAGES = {
  LOADING: 'Loading...',
} as const;

/**
 * Export all message constants
 */
export const ADMIN_MESSAGES = {
  ERROR: ADMIN_ERROR_MESSAGES,
  SUCCESS: ADMIN_SUCCESS_MESSAGES,
  CONFIRMATION: ADMIN_CONFIRMATION_MESSAGES,
  INFO: ADMIN_INFO_MESSAGES,
  VALIDATION: ADMIN_VALIDATION_MESSAGES,
  LOADING: ADMIN_LOADING_MESSAGES,
} as const;
