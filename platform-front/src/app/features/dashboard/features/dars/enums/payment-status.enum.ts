/**
 * Payment status enumeration for Dâr contributions
 *
 * Represents the various states a payment/contribution can be in.
 */
export enum PaymentStatus {
  /** Payment has been successfully completed and verified */
  PAID = 'paid',

  /** Payment is due but has not been made yet */
  PENDING = 'pending',

  /** Payment is past the due date and has not been made */
  OVERDUE = 'overdue',

  /** Payment is scheduled for a future date (not yet due) */
  FUTURE = 'future',

  /** Payment was attempted but failed */
  FAILED = 'failed',

  /** Payment was refunded */
  REFUNDED = 'refunded'
}

/**
 * Helper function to get user-friendly payment status label
 */
export function getPaymentStatusLabel(status: PaymentStatus): string {
  const labels: Record<PaymentStatus, string> = {
    [PaymentStatus.PAID]: 'Paid',
    [PaymentStatus.PENDING]: 'Pending',
    [PaymentStatus.OVERDUE]: 'Overdue',
    [PaymentStatus.FUTURE]: 'Not Due Yet',
    [PaymentStatus.FAILED]: 'Failed',
    [PaymentStatus.REFUNDED]: 'Refunded'
  };
  return labels[status];
}

/**
 * Helper function to get payment status color class for UI
 */
export function getPaymentStatusColor(status: PaymentStatus): string {
  const colors: Record<PaymentStatus, string> = {
    [PaymentStatus.PAID]: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300',
    [PaymentStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300',
    [PaymentStatus.OVERDUE]: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300',
    [PaymentStatus.FUTURE]: 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300',
    [PaymentStatus.FAILED]: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300',
    [PaymentStatus.REFUNDED]: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'
  };
  return colors[status];
}

/**
 * Helper function to get payment status icon for UI
 */
export function getPaymentStatusIcon(status: PaymentStatus): string {
  const icons: Record<PaymentStatus, string> = {
    [PaymentStatus.PAID]: 'check_circle',
    [PaymentStatus.PENDING]: 'schedule',
    [PaymentStatus.OVERDUE]: 'warning',
    [PaymentStatus.FUTURE]: 'event',
    [PaymentStatus.FAILED]: 'error',
    [PaymentStatus.REFUNDED]: 'undo'
  };
  return icons[status];
}

/**
 * Helper function to check if payment needs attention
 */
export function isPaymentActionRequired(status: PaymentStatus): boolean {
  return status === PaymentStatus.PENDING || status === PaymentStatus.OVERDUE;
}

/**
 * Helper function to check if payment is complete
 */
export function isPaymentComplete(status: PaymentStatus): boolean {
  return status === PaymentStatus.PAID;
}

/**
 * Helper function to check if payment has issues
 */
export function hasPaymentIssue(status: PaymentStatus): boolean {
  return status === PaymentStatus.OVERDUE || status === PaymentStatus.FAILED;
}
