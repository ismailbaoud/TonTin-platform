/**
 * Represents a financial transaction within a Dâr
 */
export interface Transaction {
  /** Unique identifier for the transaction */
  id: number;

  /** ID of the Dâr this transaction belongs to */
  darId: number;

  /** User ID who initiated or is associated with the transaction */
  userId: number;

  /** Display name of the user */
  userName: string;

  /** Type of transaction */
  type: 'contribution' | 'payout' | 'refund' | 'penalty';

  /** Transaction amount */
  amount: number;

  /** Current status of the transaction */
  status: 'pending' | 'completed' | 'failed' | 'cancelled';

  /** Date when the transaction occurred or was created */
  date: string;

  /** Optional description or note about the transaction */
  description?: string;

  /** Cycle number this transaction is associated with */
  cycleNumber?: number;
}
