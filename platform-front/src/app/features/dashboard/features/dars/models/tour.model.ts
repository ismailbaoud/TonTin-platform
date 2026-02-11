/**
 * Represents a tour (payout cycle) in a Dâr
 *
 * A tour represents one cycle where a specific member receives the pot.
 */
export interface Tour {
  /** Unique identifier for the tour */
  id: number;

  /** Cycle number this tour represents (1-based) */
  cycleNumber: number;

  /** User ID of the member receiving the payout */
  recipientId: number;

  /** Display name of the recipient */
  recipientName: string;

  /** Avatar URL of the recipient */
  recipientAvatar?: string;

  /** Scheduled date for this payout */
  scheduledDate: string;

  /** Actual date when the payout was made */
  payoutDate?: string;

  /** Amount to be paid out (pot size) */
  amount: number;

  /** Current status of this tour */
  status: 'upcoming' | 'in-progress' | 'completed' | 'skipped';
}
