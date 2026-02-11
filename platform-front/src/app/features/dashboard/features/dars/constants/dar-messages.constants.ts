/**
 * Error and success message constants for Dâr feature
 *
 * This file contains all user-facing messages, error messages, and success messages
 * used throughout the Dâr feature for consistency.
 */

/**
 * Error messages for Dâr operations
 */
export const DAR_ERROR_MESSAGES = {
  // General errors
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  UNAUTHORIZED: 'You are not authorized to perform this action.',
  NOT_FOUND: 'Dâr not found.',

  // Loading errors
  LOAD_DARS_FAILED: 'Failed to load Dârs. Please try again.',
  LOAD_DETAILS_FAILED: 'Failed to load Dâr details. Please try again.',
  LOAD_MEMBERS_FAILED: 'Failed to load members. Please try again.',
  LOAD_TOURS_FAILED: 'Failed to load tours. Please try again.',
  LOAD_TRANSACTIONS_FAILED: 'Failed to load transactions. Please try again.',
  LOAD_MESSAGES_FAILED: 'Failed to load messages. Please try again.',

  // Creation errors
  CREATE_FAILED: 'Failed to create Dâr. Please try again.',
  INVALID_NAME: 'Please enter a valid Dâr name (3-100 characters).',
  INVALID_CONTRIBUTION: 'Please enter a valid contribution amount.',
  INVALID_MAX_MEMBERS: 'Please enter a valid number of members (2-50).',
  INVALID_START_DATE: 'Please select a valid start date.',
  INVALID_FREQUENCY: 'Please select a valid contribution frequency.',
  START_DATE_IN_PAST: 'Start date cannot be in the past.',

  // Update errors
  UPDATE_FAILED: 'Failed to update Dâr. Please try again.',
  NOT_ORGANIZER: 'Only the organizer can update Dâr settings.',
  CANNOT_UPDATE_ACTIVE: 'Cannot update certain settings after Dâr has started.',

  // Delete errors
  DELETE_FAILED: 'Failed to delete Dâr. Please try again.',
  CANNOT_DELETE_ACTIVE: 'Cannot delete an active Dâr with members.',
  DELETE_REQUIRES_ORGANIZER: 'Only the organizer can delete a Dâr.',

  // Join errors
  JOIN_FAILED: 'Failed to join Dâr. Please try again.',
  ALREADY_MEMBER: 'You are already a member of this Dâr.',
  DAR_FULL: 'This Dâr is full and cannot accept new members.',
  INVALID_INVITE_CODE: 'Invalid or expired invite code.',
  TRUST_SCORE_TOO_LOW: 'Your trust score is too low to join this Dâr.',
  DAR_ALREADY_STARTED: 'Cannot join a Dâr that has already started.',

  // Leave errors
  LEAVE_FAILED: 'Failed to leave Dâr. Please try again.',
  ORGANIZER_CANNOT_LEAVE: 'Organizer cannot leave. Please transfer ownership or delete the Dâr.',
  CANNOT_LEAVE_ACTIVE: 'Cannot leave an active Dâr with pending contributions.',

  // Member management errors
  INVITE_FAILED: 'Failed to invite member. Please try again.',
  REMOVE_FAILED: 'Failed to remove member. Please try again.',
  INVALID_EMAIL: 'Please enter a valid email address.',
  USER_NOT_FOUND: 'User not found.',
  CANNOT_REMOVE_SELF: 'Cannot remove yourself. Use "Leave Dâr" instead.',
  MEMBER_HAS_PENDING_TURN: 'Cannot remove member with pending turn.',

  // Turn order errors
  UPDATE_TURN_ORDER_FAILED: 'Failed to update turn order. Please try again.',
  INVALID_TURN_ORDER: 'Invalid turn order configuration.',
  CANNOT_CHANGE_PAST_TURNS: 'Cannot change turn order for completed cycles.',

  // Payment errors
  PAYMENT_FAILED: 'Payment failed. Please try again.',
  INSUFFICIENT_BALANCE: 'Insufficient balance to make contribution.',
  PAYMENT_ALREADY_MADE: 'Contribution for this cycle has already been paid.',
  PAYMENT_NOT_DUE: 'Payment is not yet due.',

  // Messaging errors
  SEND_MESSAGE_FAILED: 'Failed to send message. Please try again.',
  EMPTY_MESSAGE: 'Message cannot be empty.',
  MESSAGE_TOO_LONG: 'Message is too long (max 1000 characters).',

  // Start Dâr errors
  START_FAILED: 'Failed to start Dâr. Please try again.',
  INSUFFICIENT_MEMBERS: 'Cannot start Dâr with less than minimum required members.',
  DAR_ALREADY_ACTIVE: 'This Dâr is already active.',

  // Report errors
  REPORT_FAILED: 'Failed to report member. Please try again.',
  REPORT_REASON_REQUIRED: 'Please provide a reason for reporting.',
  CANNOT_REPORT_SELF: 'Cannot report yourself.',

  // Invite code errors
  GENERATE_INVITE_CODE_FAILED: 'Failed to generate invite code. Please try again.',
} as const;

/**
 * Success messages for Dâr operations
 */
export const DAR_SUCCESS_MESSAGES = {
  // CRUD operations
  CREATE_SUCCESS: 'Dâr created successfully!',
  UPDATE_SUCCESS: 'Dâr updated successfully!',
  DELETE_SUCCESS: 'Dâr deleted successfully.',

  // Membership operations
  JOIN_SUCCESS: 'You have successfully joined the Dâr!',
  LEAVE_SUCCESS: 'You have left the Dâr.',
  INVITE_SUCCESS: 'Invitation sent successfully!',
  REMOVE_MEMBER_SUCCESS: 'Member removed successfully.',

  // Turn order
  UPDATE_TURN_ORDER_SUCCESS: 'Turn order updated successfully!',

  // Payment operations
  PAYMENT_SUCCESS: 'Contribution paid successfully!',
  PAYOUT_SUCCESS: 'Payout completed successfully!',

  // Messaging
  MESSAGE_SENT: 'Message sent!',

  // Start Dâr
  START_SUCCESS: 'Dâr started successfully!',

  // Reporting
  REPORT_SUCCESS: 'Member reported. Our team will review this report.',

  // Invite code
  INVITE_CODE_GENERATED: 'New invite code generated successfully!',
  INVITE_CODE_COPIED: 'Invite code copied to clipboard!',

  // General
  CHANGES_SAVED: 'Changes saved successfully!',
  ACTION_COMPLETED: 'Action completed successfully!',
} as const;

/**
 * Confirmation messages for Dâr operations
 */
export const DAR_CONFIRMATION_MESSAGES = {
  DELETE_DAR: 'Are you sure you want to delete this Dâr? This action cannot be undone.',
  LEAVE_DAR: 'Are you sure you want to leave this Dâr?',
  REMOVE_MEMBER: 'Are you sure you want to remove this member from the Dâr?',
  START_DAR: 'Are you sure you want to start this Dâr? Once started, some settings cannot be changed.',
  CANCEL_DAR: 'Are you sure you want to cancel this Dâr? All members will be notified.',
  REPORT_MEMBER: 'Are you sure you want to report this member? This will affect their trust score.',
} as const;

/**
 * Info messages for Dâr operations
 */
export const DAR_INFO_MESSAGES = {
  NO_DARS: 'You are not part of any Dârs yet. Create or join one to get started!',
  NO_ACTIVE_DARS: 'You have no active Dârs at the moment.',
  NO_PENDING_DARS: 'You have no pending Dârs.',
  NO_COMPLETED_DARS: 'You have no completed Dârs.',
  NO_MEMBERS: 'No members yet. Invite people to join!',
  NO_TRANSACTIONS: 'No transactions yet.',
  NO_MESSAGES: 'No messages yet. Start the conversation!',
  NO_TOURS: 'No tours scheduled yet.',
  NO_SEARCH_RESULTS: 'No Dârs found matching your search.',

  PENDING_APPROVAL: 'Your request to join is pending approval from the organizer.',
  WAITING_FOR_MEMBERS: 'Waiting for more members to join before starting.',
  DAR_FULL: 'This Dâr has reached maximum capacity.',
  PRIVATE_DAR: 'This is a private Dâr. You need an invite code to join.',

  PAYMENT_DUE_SOON: 'Your next contribution is due soon.',
  PAYMENT_OVERDUE: 'You have an overdue payment. Please pay as soon as possible.',
  YOUR_TURN_NEXT: 'Your turn to receive the pot is coming up!',
  YOUR_TURN_NOW: 'It\'s your turn to receive the pot!',

  LOW_TRUST_SCORE: 'Your trust score is low. Complete your contributions on time to improve it.',
} as const;

/**
 * Validation messages for form fields
 */
export const DAR_VALIDATION_MESSAGES = {
  NAME_REQUIRED: 'Dâr name is required',
  NAME_MIN_LENGTH: 'Dâr name must be at least 3 characters',
  NAME_MAX_LENGTH: 'Dâr name cannot exceed 100 characters',

  CONTRIBUTION_REQUIRED: 'Contribution amount is required',
  CONTRIBUTION_MIN: 'Contribution amount must be at least 1,000',
  CONTRIBUTION_MAX: 'Contribution amount cannot exceed 10,000,000',
  CONTRIBUTION_POSITIVE: 'Contribution amount must be positive',

  MAX_MEMBERS_REQUIRED: 'Maximum members is required',
  MAX_MEMBERS_MIN: 'At least 2 members are required',
  MAX_MEMBERS_MAX: 'Cannot exceed 50 members',

  FREQUENCY_REQUIRED: 'Contribution frequency is required',
  START_DATE_REQUIRED: 'Start date is required',
  VISIBILITY_REQUIRED: 'Visibility is required',

  DESCRIPTION_MAX_LENGTH: 'Description cannot exceed 500 characters',
  RULES_MAX_LENGTH: 'Rules cannot exceed 2000 characters',

  EMAIL_REQUIRED: 'Email is required',
  EMAIL_INVALID: 'Please enter a valid email address',

  MESSAGE_REQUIRED: 'Message cannot be empty',
  MESSAGE_MAX_LENGTH: 'Message cannot exceed 1000 characters',

  REASON_REQUIRED: 'Reason is required',
  REASON_MIN_LENGTH: 'Reason must be at least 10 characters',
} as const;

/**
 * Loading messages for async operations
 */
export const DAR_LOADING_MESSAGES = {
  LOADING_DARS: 'Loading Dârs...',
  LOADING_DETAILS: 'Loading details...',
  LOADING_MEMBERS: 'Loading members...',
  LOADING_TRANSACTIONS: 'Loading transactions...',
  LOADING_MESSAGES: 'Loading messages...',
  CREATING_DAR: 'Creating Dâr...',
  UPDATING_DAR: 'Updating Dâr...',
  DELETING_DAR: 'Deleting Dâr...',
  JOINING_DAR: 'Joining Dâr...',
  LEAVING_DAR: 'Leaving Dâr...',
  PROCESSING_PAYMENT: 'Processing payment...',
  SENDING_MESSAGE: 'Sending message...',
  SENDING_INVITE: 'Sending invite...',
  STARTING_DAR: 'Starting Dâr...',
} as const;

/**
 * Tooltip messages for UI elements
 */
export const DAR_TOOLTIP_MESSAGES = {
  ORGANIZER_BADGE: 'Organizer of this Dâr',
  CO_ORGANIZER_BADGE: 'Co-Organizer with administrative privileges',
  TRUST_SCORE: 'Trust score based on payment history and member reports',
  TURN_ORDER: 'Position in the payout queue',
  CONTRIBUTION_AMOUNT: 'Amount each member contributes per cycle',
  POT_SIZE: 'Total amount each member receives on their turn',
  NEXT_PAYOUT: 'When the next payout will occur',
  INVITE_CODE: 'Share this code with others to invite them',
  PRIVATE_DAR: 'Only invited members can join',
  PUBLIC_DAR: 'Anyone can discover and join',
  INVITE_ONLY: 'Members can only join with an invite code',
} as const;

/**
 * Export all message constants as a single object
 */
export const DAR_MESSAGES = {
  ERROR: DAR_ERROR_MESSAGES,
  SUCCESS: DAR_SUCCESS_MESSAGES,
  CONFIRMATION: DAR_CONFIRMATION_MESSAGES,
  INFO: DAR_INFO_MESSAGES,
  VALIDATION: DAR_VALIDATION_MESSAGES,
  LOADING: DAR_LOADING_MESSAGES,
  TOOLTIP: DAR_TOOLTIP_MESSAGES,
} as const;
