import { MemberRole } from "../enums/member-role.enum";
import { MemberStatus } from "../enums/member-status.enum";

/**
 * Represents a member of a Dâr
 *
 * This model represents the relationship between a User and a Dâr.
 * It tracks the member's participation status (invitation acceptance)
 * and their role within the Dâr.
 *
 * NOTE: Payment/contribution tracking is handled separately and is NOT
 * part of this model. MemberStatus is about participation, not payments.
 */
export interface Member {
  /** Unique identifier for the member record (UUID) */
  id: string;

  /** User ID of the member (UUID) */
  userId: string;

  /** Display name of the member */
  userName: string;

  /** Email address of the member */
  email: string;

  /** Avatar URL for the member (optional) */
  avatar?: string;

  /** Role/permission of the member within this Dâr (ORGANIZER or MEMBER) */
  role: MemberRole;

  /**
   * Status of member's participation in the Dâr
   * PENDING = Invited but not accepted
   * ACTIVE = Accepted invitation and participating
   * LEAVED = Left or removed from Dâr
   */
  status: MemberStatus;

  /** Date when the member joined the Dâr */
  joinedDate: string;

  /** Date when the member was created/invited */
  createdAt?: string;

  /** Date when the member record was last updated */
  updatedAt?: string;

  /** Position in the turn order (1-based) - for rotation scheduling */
  turnOrder?: number;

  /** Scheduled date for this member's turn to receive the pot */
  turnDate?: string;

  /** Trust score of this member (0-100) - optional */
  trustScore?: number;
}

/**
 * Extended member interface that includes payment information
 * Use this when displaying members with their payment status
 */
export interface MemberWithPayment extends Member {
  /** Current payment/contribution status for this member */
  paymentStatus: "paid" | "pending" | "overdue" | "future" | "failed";

  /** Amount this member has contributed */
  contributionAmount?: number;

  /** Number of payments made */
  paymentsMade?: number;

  /** Total payments expected */
  paymentsExpected?: number;
}
