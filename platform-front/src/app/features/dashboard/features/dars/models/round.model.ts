/**
 * Round model representing a payment round in a Dâr
 */
export interface Round {
  /** Unique identifier of the round */
  id: string;

  /** Round number (1-based) */
  number: number;

  /** Current status of the round */
  status: RoundStatus;

  /** Date when the round occurs */
  date: string;

  /** Amount for this round */
  amount: number;

  /** ID of the associated dart */
  dartId: string;

  /** Name of the associated dart */
  dartName?: string;

  /** ID of the member who receives money in this round */
  recipientMemberId?: string;

  /** Name of the member who receives money in this round */
  recipientMemberName?: string;

  /** Email of the member who receives money in this round */
  recipientMemberEmail?: string;

  /** Timestamp when the round was created */
  createdAt: string;

  /** Timestamp when the round was last updated */
  updatedAt: string;
}

/**
 * Round status enumeration
 */
export enum RoundStatus {
  /** Round has been fully paid */
  PAYED = 'PAYED',
  /** Round is pending payment */
  INPAYED = 'INPAYED'
}

/**
 * Round statistics for a dart
 */
export interface RoundStatistics {
  /** Total number of rounds */
  totalRounds: number;
  /** Number of paid rounds */
  paidRounds: number;
  /** Number of unpaid rounds */
  unpaidRounds: number;
  /** Current round number (null if all paid) */
  currentRoundNumber: number | null;
}

/**
 * Helper function to get user-friendly status label
 */
export function getRoundStatusLabel(status: RoundStatus): string {
  const labels: Record<RoundStatus, string> = {
    [RoundStatus.PAYED]: 'Paid',
    [RoundStatus.INPAYED]: 'Pending Payment'
  };
  return labels[status] || status;
}

/**
 * Helper function to get status color class
 */
export function getRoundStatusColor(status: RoundStatus): string {
  const colors: Record<RoundStatus, string> = {
    [RoundStatus.PAYED]: 'bg-green-50 dark:bg-green-900/30 text-green-700 dark:text-green-400 border-green-600/20',
    [RoundStatus.INPAYED]: 'bg-yellow-50 dark:bg-yellow-900/30 text-yellow-800 dark:text-yellow-400 border-yellow-600/20'
  };
  return colors[status] || '';
}

/**
 * Helper function to check if round is paid
 */
export function isRoundPaid(status: RoundStatus): boolean {
  return status === RoundStatus.PAYED;
}

/**
 * Helper function to check if round is pending
 */
export function isRoundPending(status: RoundStatus): boolean {
  return status === RoundStatus.INPAYED;
}

/**
 * Format date for display
 */
export function formatRoundDate(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
}

/**
 * Format date with time for display
 */
export function formatRoundDateTime(dateString: string): string {
  const date = new Date(dateString);
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Get relative time (e.g., "in 3 days", "2 weeks ago")
 */
export function getRelativeTime(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffMs = date.getTime() - now.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) {
    return 'Today';
  } else if (diffDays === 1) {
    return 'Tomorrow';
  } else if (diffDays === -1) {
    return 'Yesterday';
  } else if (diffDays > 0) {
    if (diffDays < 7) {
      return `in ${diffDays} days`;
    } else if (diffDays < 30) {
      const weeks = Math.floor(diffDays / 7);
      return `in ${weeks} week${weeks > 1 ? 's' : ''}`;
    } else {
      const months = Math.floor(diffDays / 30);
      return `in ${months} month${months > 1 ? 's' : ''}`;
    }
  } else {
    const absDays = Math.abs(diffDays);
    if (absDays < 7) {
      return `${absDays} days ago`;
    } else if (absDays < 30) {
      const weeks = Math.floor(absDays / 7);
      return `${weeks} week${weeks > 1 ? 's' : ''} ago`;
    } else {
      const months = Math.floor(absDays / 30);
      return `${months} month${months > 1 ? 's' : ''} ago`;
    }
  }
}
