/**
 * Dâr lifecycle status enumeration
 *
 * Represents the various states a Dâr can be in throughout its lifecycle.
 */
export enum DarStatus {
  /** Dâr is being set up, waiting for members to join */
  PENDING = 'pending',

  /** Dâr is active and running, contributions are being collected */
  ACTIVE = 'active',

  /** Dâr has completed all cycles successfully */
  COMPLETED = 'completed',

  /** Dâr was cancelled before completion */
  CANCELLED = 'cancelled'
}

/**
 * Helper function to get user-friendly status label
 */
export function getDarStatusLabel(status: DarStatus): string {
  const labels: Record<DarStatus, string> = {
    [DarStatus.PENDING]: 'Pending',
    [DarStatus.ACTIVE]: 'Active',
    [DarStatus.COMPLETED]: 'Completed',
    [DarStatus.CANCELLED]: 'Cancelled'
  };
  return labels[status];
}

/**
 * Helper function to get status color class for UI
 */
export function getDarStatusColor(status: DarStatus): string {
  const colors: Record<DarStatus, string> = {
    [DarStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300',
    [DarStatus.ACTIVE]: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300',
    [DarStatus.COMPLETED]: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300',
    [DarStatus.CANCELLED]: 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300'
  };
  return colors[status];
}
