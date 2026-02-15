import { DarStatus } from "../enums/dar-status.enum";
import { DarFrequency } from "../enums/dar-frequency.enum";

/**
 * Represents a Dâr (rotating savings and credit association)
 */
export interface Dar {
  /** Unique identifier for the Dâr (UUID string) */
  id: string;

  /** Name of the Dâr */
  name: string;

  /** Description of the Dâr's purpose and rules */
  description?: string;

  /** Cover image URL for the Dâr */
  image?: string;

  /** Current lifecycle status of the Dâr */
  status: DarStatus;

  /** User ID of the Dâr organizer (UUID string) */
  organizerId: string;

  /** Display name of the organizer */
  organizerName: string;

  /** Avatar URL of the organizer */
  organizerAvatar?: string;

  /** Date when the Dâr was created */
  createdDate: string;

  /** Date when the Dâr started (first contribution cycle) */
  startDate?: string;

  /** Date when the Dâr ended or is expected to end */
  endDate?: string;

  /** Current cycle/round number (1-based) */
  currentCycle: number;

  /** Total number of cycles planned */
  totalCycles: number;

  /** Current number of members */
  memberCount: number;

  /** Maximum number of members allowed */
  maxMembers: number;

  /** Amount each member contributes per cycle */
  monthlyContribution: number;

  /** Total pot size (monthlyContribution * memberCount) */
  totalMonthlyPool: number;

  /** How often contributions are made */
  paymentFrequency: DarFrequency;

  /** Order method */
  orderMethod: string;

  /** Date of the next scheduled payout */
  nextPayoutDate?: string;

  /** Name of the member receiving the next payout */
  nextPayoutRecipient?: string;

  /** Whether the current user is the organizer */
  isOrganizer: boolean;

  /** Whether the current user is a member */
  isMember: boolean;

  /** Current user's permission level in this dart (ORGANIZER or MEMBER) */
  userPermission?: string;

  /** Current user's member status (PENDING, ACTIVE, LEAVED) */
  userMemberStatus?: string;

  /** Invite code for joining (organizer only) */
  inviteCode?: string;

  /** Custom rules and terms for this Dâr */
  rules?: string;

  /** Visibility level of the Dâr */
  visibility: "public" | "private" | "invite-only";

  /** Total number of members (alias for memberCount) */
  totalMembers?: number;

  /** Contribution amount (alias for monthlyContribution) */
  contributionAmount?: number;

  /** Total pot size (alias for totalMonthlyPool) */
  potSize?: number;
}
