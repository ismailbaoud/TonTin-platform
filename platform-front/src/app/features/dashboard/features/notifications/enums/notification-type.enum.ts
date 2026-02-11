/**
 * Notification type enumeration
 *
 * Represents the various types of notifications that can be sent to users.
 */
export enum NotificationType {
  /** Payment is due soon or overdue */
  PAYMENT_DUE = 'payment_due',

  /** Payment has been received/confirmed */
  PAYMENT_RECEIVED = 'payment_received',

  /** User's payout is ready to be disbursed */
  PAYOUT_READY = 'payout_ready',

  /** User has been invited to join a Dâr */
  DAR_INVITATION = 'dar_invitation',

  /** A new member has joined a Dâr */
  MEMBER_JOINED = 'member_joined',

  /** A member has left a Dâr */
  MEMBER_LEFT = 'member_left',

  /** A tour/cycle has been completed */
  TOUR_COMPLETED = 'tour_completed',

  /** General reminder notification */
  REMINDER = 'reminder',

  /** System-level notification */
  SYSTEM = 'system',

  /** Trust score update or change */
  TRUST_SCORE = 'trust_score',

  /** New message in a Dâr chat */
  MESSAGE = 'message'
}

/**
 * Helper function to get user-friendly notification type label
 */
export function getNotificationTypeLabel(type: NotificationType): string {
  const labels: Record<NotificationType, string> = {
    [NotificationType.PAYMENT_DUE]: 'Payment Due',
    [NotificationType.PAYMENT_RECEIVED]: 'Payment Received',
    [NotificationType.PAYOUT_READY]: 'Payout Ready',
    [NotificationType.DAR_INVITATION]: 'Dâr Invitation',
    [NotificationType.MEMBER_JOINED]: 'Member Joined',
    [NotificationType.MEMBER_LEFT]: 'Member Left',
    [NotificationType.TOUR_COMPLETED]: 'Tour Completed',
    [NotificationType.REMINDER]: 'Reminder',
    [NotificationType.SYSTEM]: 'System',
    [NotificationType.TRUST_SCORE]: 'Trust Score',
    [NotificationType.MESSAGE]: 'Message'
  };
  return labels[type];
}

/**
 * Helper function to get notification type icon (Material Icons)
 */
export function getNotificationTypeIcon(type: NotificationType): string {
  const icons: Record<NotificationType, string> = {
    [NotificationType.PAYMENT_DUE]: 'schedule',
    [NotificationType.PAYMENT_RECEIVED]: 'payments',
    [NotificationType.PAYOUT_READY]: 'account_balance_wallet',
    [NotificationType.DAR_INVITATION]: 'group_add',
    [NotificationType.MEMBER_JOINED]: 'person_add',
    [NotificationType.MEMBER_LEFT]: 'person_remove',
    [NotificationType.TOUR_COMPLETED]: 'check_circle',
    [NotificationType.REMINDER]: 'notifications_active',
    [NotificationType.SYSTEM]: 'info',
    [NotificationType.TRUST_SCORE]: 'verified',
    [NotificationType.MESSAGE]: 'message'
  };
  return icons[type];
}

/**
 * Helper function to get notification type color class
 */
export function getNotificationTypeColor(type: NotificationType): string {
  const colors: Record<NotificationType, string> = {
    [NotificationType.PAYMENT_DUE]: 'text-orange-600',
    [NotificationType.PAYMENT_RECEIVED]: 'text-green-600',
    [NotificationType.PAYOUT_READY]: 'text-blue-600',
    [NotificationType.DAR_INVITATION]: 'text-purple-600',
    [NotificationType.MEMBER_JOINED]: 'text-green-600',
    [NotificationType.MEMBER_LEFT]: 'text-gray-600',
    [NotificationType.TOUR_COMPLETED]: 'text-green-600',
    [NotificationType.REMINDER]: 'text-yellow-600',
    [NotificationType.SYSTEM]: 'text-blue-600',
    [NotificationType.TRUST_SCORE]: 'text-indigo-600',
    [NotificationType.MESSAGE]: 'text-blue-600'
  };
  return colors[type];
}

/**
 * Helper function to check if notification type requires immediate action
 */
export function requiresAction(type: NotificationType): boolean {
  return type === NotificationType.PAYMENT_DUE ||
         type === NotificationType.DAR_INVITATION;
}

/**
 * Helper function to check if notification is informational only
 */
export function isInformational(type: NotificationType): boolean {
  return type === NotificationType.SYSTEM ||
         type === NotificationType.TOUR_COMPLETED ||
         type === NotificationType.MEMBER_JOINED;
}
