import { Dar } from './dar.model';
import { Member } from './member.model';
import { Tour } from './tour.model';
import { Transaction } from './transaction.model';
import { Message } from './message.model';

/**
 * Represents detailed information about a Dâr, extending the base Dâr model
 * with additional related data such as members, tours, transactions, and messages.
 *
 * This is typically used on the Dâr details page where comprehensive information
 * about the Dâr and its activities is displayed.
 */
export interface DarDetails extends Dar {
  /** List of all members participating in this Dâr */
  members: Member[];

  /** List of all tours (payout cycles) for this Dâr */
  tours: Tour[];

  /** List of recent transactions for this Dâr */
  transactions: Transaction[];

  /** List of recent messages in this Dâr's chat */
  messages: Message[];
}
