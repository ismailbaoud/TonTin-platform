/**
 * Notification priority enumeration
 *
 * Represents the urgency level of a notification.
 */
export enum NotificationPriority {
  /** Low priority - informational, can be checked at user's convenience */
  LOW = 'low',

  /** Medium priority - should be checked soon */
  MEDIUM = 'medium',

  /** High priority - requires prompt attention */
  HIGH = 'high',

  /** Urgent priority - requires immediate action */
  URGENT = 'urgent'
}

/**
 * Helper function to get user-friendly priority label
 */
export function getNotificationPriorityLabel(priority: NotificationPriority): string {
  const labels: Record<NotificationPriority, string> = {
    [NotificationPriority.LOW]: 'Low',
    [NotificationPriority.MEDIUM]: 'Medium',
    [NotificationPriority.HIGH]: 'High',
    [NotificationPriority.URGENT]: 'Urgent'
  };
  return labels[priority];
}

/**
 * Helper function to get priority color class
 */
export function getNotificationPriorityColor(priority: NotificationPriority): string {
  const colors: Record<NotificationPriority, string> = {
    [NotificationPriority.LOW]: 'text-gray-600 dark:text-gray-400',
    [NotificationPriority.MEDIUM]: 'text-blue-600 dark:text-blue-400',
    [NotificationPriority.HIGH]: 'text-orange-600 dark:text-orange-400',
    [NotificationPriority.URGENT]: 'text-red-600 dark:text-red-400'
  };
  return colors[priority];
}

/**
 * Helper function to get priority badge color class
 */
export function getNotificationPriorityBadgeColor(priority: NotificationPriority): string {
  const colors: Record<NotificationPriority, string> = {
    [NotificationPriority.LOW]: 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300',
    [NotificationPriority.MEDIUM]: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300',
    [NotificationPriority.HIGH]: 'bg-orange-100 text-orange-800 dark:bg-orange-900/30 dark:text-orange-300',
    [NotificationPriority.URGENT]: 'bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300'
  };
  return colors[priority];
}

/**
 * Helper function to check if priority requires immediate attention
 */
export function isUrgent(priority: NotificationPriority): boolean {
  return priority === NotificationPriority.URGENT;
}

/**
 * Helper function to check if priority is high or urgent
 */
export function isHighPriority(priority: NotificationPriority): boolean {
  return priority === NotificationPriority.HIGH || priority === NotificationPriority.URGENT;
}

/**
 * Helper function to sort notifications by priority
 */
export function comparePriority(a: NotificationPriority, b: NotificationPriority): number {
  const priorityOrder = {
    [NotificationPriority.URGENT]: 4,
    [NotificationPriority.HIGH]: 3,
    [NotificationPriority.MEDIUM]: 2,
    [NotificationPriority.LOW]: 1
  };
  return priorityOrder[b] - priorityOrder[a]; // Higher priority first
}
