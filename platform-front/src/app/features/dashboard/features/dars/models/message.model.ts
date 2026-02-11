/**
 * Represents a message in a Dâr's communication channel
 */
export interface Message {
  /** Unique identifier for the message */
  id: number;

  /** ID of the Dâr this message belongs to */
  darId: number;

  /** User ID of the message sender */
  userId: number;

  /** Display name of the sender */
  userName: string;

  /** Avatar URL of the sender */
  userAvatar?: string;

  /** Message content/text */
  content: string;

  /** Timestamp when the message was sent */
  timestamp: string;

  /** Whether this is an automated system message */
  isSystemMessage: boolean;
}
