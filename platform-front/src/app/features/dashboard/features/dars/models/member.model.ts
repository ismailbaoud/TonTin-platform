import { MemberRole } from '../enums/member-role.enum';
import { PaymentStatus } from '../enums/payment-status.enum';

/**
 * Represents a member of a Dâr
 */
export interface Member {
  /** Unique identifier for the member record */
  id: number;

  /** User ID of the member */
  userId: number;

  /** Display name of the member */
  userName: string;

  /** Email address of the member */
  email: string;

  /** Avatar URL for the member (optional) */
  avatar?: string;

  /** Role of the member within this Dâr */
  role: MemberRole;

  /** Date when the member joined the Dâr */
  joinedDate: string;

  /** Position in the turn order (1-based) */
  turnOrder: number;

  /** Scheduled date for this member's turn to receive the pot */
  turnDate?: string;

  /** Current payment status for this member */
  paymentStatus: PaymentStatus;

  /** Amount this member has contributed */
  contributionAmount?: number;

  /** Trust score of this member (0-100) */
  trustScore?: number;
}
