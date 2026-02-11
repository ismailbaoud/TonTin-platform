import { DarFrequency } from "../enums/dar-frequency.enum";

/**
 * Request payload for creating a new Dâr
 */
export interface CreateDarRequest {
  /** Name of the Dâr */
  name: string;

  /** Optional description of the Dâr's purpose */
  description?: string;

  /** Optional cover image URL */
  image?: string;

  /** Amount each member contributes per cycle */
  contributionAmount: number;

  /** How often contributions are made */
  frequency: DarFrequency;

  /** Method for allocating payouts to members */
  allocationMethod: "random" | "sequential" | "bidding";

  /** Optional custom rules and terms */
  rules?: string;
}

/**
 * Request payload for updating an existing Dâr
 * Only organizers can update Dâr information
 */
export interface UpdateDarRequest {
  /** Updated name of the Dâr */
  name?: string;

  /** Updated description */
  description?: string;

  /** Updated cover image URL */
  image?: string;

  /** Updated custom rules and terms */
  rules?: string;

  /** Updated visibility level */
  visibility?: "public" | "private" | "invite-only";
}

/**
 * Request payload for inviting a member to a Dâr
 * Organizers can invite by email or user ID
 */
export interface InviteMemberRequest {
  /** ID of the Dâr to invite the member to */
  darId: number;

  /** Email address of the person to invite (optional if userId provided) */
  email?: string;

  /** User ID of the person to invite (optional if email provided) */
  userId?: number;
}

/**
 * Request payload for joining a Dâr
 * Users can join by Dâr ID (public) or invite code (private/invite-only)
 */
export interface JoinDarRequest {
  /** ID of the Dâr to join (for public Dârs) */
  darId?: number;

  /** Invite code for joining (for private/invite-only Dârs) */
  inviteCode?: string;
}

/**
 * Request payload for updating member turn order
 * Only organizers can update the turn order
 */
export interface UpdateTurnOrderRequest {
  /** Array of member IDs with their new turn order positions */
  memberOrder: MemberOrder[];
}

/**
 * Represents a member's position in the turn order
 */
export interface MemberOrder {
  /** Member ID */
  memberId: number;

  /** New position in turn order (1-based) */
  order: number;
}

/**
 * Request payload for reporting a member
 */
export interface ReportMemberRequest {
  /** ID of the Dâr */
  darId: number;

  /** ID of the member being reported */
  memberId: number;

  /** Reason for the report */
  reason: string;
}

/**
 * Request payload for sending a message in a Dâr
 */
export interface SendMessageRequest {
  /** Message content/text */
  content: string;
}

/**
 * Response payload for generating an invite code
 */
export interface GenerateInviteCodeResponse {
  /** The newly generated invite code */
  inviteCode: string;
}
