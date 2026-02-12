#!/bin/bash

# Script to create the models/enums/constants structure for a feature
# Usage: ./create-feature-structure.sh <feature-name>

set -e

FEATURE=$1

if [ -z "$FEATURE" ]; then
  echo "Usage: ./create-feature-structure.sh <feature-name>"
  echo "Example: ./create-feature-structure.sh notifications"
  exit 1
fi

BASE_PATH="src/app/features/dashboard/features/$FEATURE"

if [ ! -d "$BASE_PATH" ]; then
  echo "Error: Feature directory does not exist: $BASE_PATH"
  exit 1
fi

echo "Creating structure for $FEATURE feature..."

# Create directories
mkdir -p "$BASE_PATH/models"
mkdir -p "$BASE_PATH/enums"
mkdir -p "$BASE_PATH/constants"

# Create models index
if [ ! -f "$BASE_PATH/models/index.ts" ]; then
cat > "$BASE_PATH/models/index.ts" << 'EOF'
/**
 * Barrel export file for models
 *
 * This file re-exports all model interfaces and types for easier importing
 * throughout the application.
 */

// Export all models here
// Example:
// export * from './example.model';
EOF
fi

# Create enums index
if [ ! -f "$BASE_PATH/enums/index.ts" ]; then
cat > "$BASE_PATH/enums/index.ts" << 'EOF'
/**
 * Barrel export file for enums
 *
 * This file re-exports all enums and their helper functions for easier importing
 * throughout the application.
 */

// Export all enums here
// Example:
// export * from './example.enum';
EOF
fi

# Create constants index
if [ ! -f "$BASE_PATH/constants/index.ts" ]; then
cat > "$BASE_PATH/constants/index.ts" << 'EOF'
/**
 * Barrel export file for constants
 *
 * This file re-exports all constants for easier importing
 * throughout the application.
 */

// Export all constants here
// Example:
// export * from './example.constants';
EOF
fi

# Create config constants file
if [ ! -f "$BASE_PATH/constants/${FEATURE}-config.constants.ts" ]; then
cat > "$BASE_PATH/constants/${FEATURE}-config.constants.ts" << EOF
/**
 * Configuration constants for ${FEATURE} feature
 *
 * This file contains all the configuration values, magic numbers,
 * and constant values used throughout the ${FEATURE} feature.
 */

/**
 * Pagination settings
 */
export const ${FEATURE^^}_PAGINATION = {
  /** Default page size */
  DEFAULT_PAGE_SIZE: 20,

  /** Maximum page size allowed */
  MAX_PAGE_SIZE: 100,
} as const;

/**
 * Timing constants
 */
export const ${FEATURE^^}_TIMING = {
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
export const ${FEATURE^^}_FEATURES = {
  /** Enable/disable specific features */
  ENABLE_FEATURE_X: true,
} as const;

/**
 * Export all constants as a single object
 */
export const ${FEATURE^^}_CONFIG = {
  PAGINATION: ${FEATURE^^}_PAGINATION,
  TIMING: ${FEATURE^^}_TIMING,
  FEATURES: ${FEATURE^^}_FEATURES,
} as const;
EOF
fi

# Create messages constants file
if [ ! -f "$BASE_PATH/constants/${FEATURE}-messages.constants.ts" ]; then
cat > "$BASE_PATH/constants/${FEATURE}-messages.constants.ts" << EOF
/**
 * Error and success message constants for ${FEATURE} feature
 *
 * This file contains all user-facing messages for consistency.
 */

/**
 * Error messages
 */
export const ${FEATURE^^}_ERROR_MESSAGES = {
  UNKNOWN_ERROR: 'An unexpected error occurred. Please try again.',
  NETWORK_ERROR: 'Network error. Please check your connection and try again.',
  LOAD_FAILED: 'Failed to load data. Please try again.',
} as const;

/**
 * Success messages
 */
export const ${FEATURE^^}_SUCCESS_MESSAGES = {
  ACTION_SUCCESS: 'Action completed successfully!',
  CHANGES_SAVED: 'Changes saved successfully!',
} as const;

/**
 * Confirmation messages
 */
export const ${FEATURE^^}_CONFIRMATION_MESSAGES = {
  CONFIRM_ACTION: 'Are you sure you want to perform this action?',
} as const;

/**
 * Info messages
 */
export const ${FEATURE^^}_INFO_MESSAGES = {
  NO_DATA: 'No data available.',
} as const;

/**
 * Validation messages
 */
export const ${FEATURE^^}_VALIDATION_MESSAGES = {
  REQUIRED_FIELD: 'This field is required',
} as const;

/**
 * Loading messages
 */
export const ${FEATURE^^}_LOADING_MESSAGES = {
  LOADING: 'Loading...',
} as const;

/**
 * Export all message constants
 */
export const ${FEATURE^^}_MESSAGES = {
  ERROR: ${FEATURE^^}_ERROR_MESSAGES,
  SUCCESS: ${FEATURE^^}_SUCCESS_MESSAGES,
  CONFIRMATION: ${FEATURE^^}_CONFIRMATION_MESSAGES,
  INFO: ${FEATURE^^}_INFO_MESSAGES,
  VALIDATION: ${FEATURE^^}_VALIDATION_MESSAGES,
  LOADING: ${FEATURE^^}_LOADING_MESSAGES,
} as const;
EOF
fi

# Update constants index to include the new files
cat > "$BASE_PATH/constants/index.ts" << EOF
/**
 * Barrel export file for ${FEATURE} constants
 *
 * This file re-exports all constants for easier importing
 * throughout the application.
 */

export * from './${FEATURE}-config.constants';
export * from './${FEATURE}-messages.constants';
EOF

echo "✅ Created structure for $FEATURE feature:"
echo "   - models/ (with index.ts)"
echo "   - enums/ (with index.ts)"
echo "   - constants/ (with index.ts, ${FEATURE}-config.constants.ts, ${FEATURE}-messages.constants.ts)"
echo ""
echo "Next steps:"
echo "1. Extract interfaces from service files to models/"
echo "2. Create enums for string unions"
echo "3. Add specific constants to config and messages files"
echo "4. Update service to import from new files"
echo "5. Update barrel exports in index.ts files"
