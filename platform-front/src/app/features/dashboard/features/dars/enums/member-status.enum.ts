/**
 * Member status enumeration for Dâr membership
 *
 * Represents the status of a member's participation in a Dâr.
 * This indicates whether the member has accepted the invitation or left the Dâr.
 *
 * NOTE: This is SEPARATE from PaymentStatus, which tracks whether contributions have been paid.
 */
export enum MemberStatus {
  /** Member has been invited or requested to join but not yet confirmed/accepted */
  PENDING = 'PENDING',

  /** Member is actively participating in the Dâr (has accepted invitation) */
  ACTIVE = 'ACTIVE',

  /** Member has left or been removed from the Dâr */
  LEAVED = 'LEAVED'
}

/**
 * Helper function to get user-friendly member status label
 */
export function getMemberStatusLabel(status: MemberStatus): string {
  const labels: Record<MemberStatus, string> = {
    [MemberStatus.PENDING]: 'Pending',
    [MemberStatus.ACTIVE]: 'Active',
    [MemberStatus.LEAVED]: 'Left'
  };
  return labels[status];
}

/**
 * Helper function to get member status color class for UI
 */
export function getMemberStatusColor(status: MemberStatus): string {
  const colors: Record<MemberStatus, string> = {
    [MemberStatus.PENDING]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300',
    [MemberStatus.ACTIVE]: 'bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300',
    [MemberStatus.LEAVED]: 'bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300'
  };
  return colors[status];
}

/**
 * Helper function to get member status icon for UI
 */
export function getMemberStatusIcon(status: MemberStatus): string {
  const icons: Record<MemberStatus, string> = {
    [MemberStatus.PENDING]: 'schedule',
    [MemberStatus.ACTIVE]: 'check_circle',
    [MemberStatus.LEAVED]: 'logout'
  };
  return icons[status];
}

/**
 * Helper function to check if member can participate
 */
export function canParticipate(status: MemberStatus): boolean {
  return status === MemberStatus.ACTIVE;
}

/**
 * Helper function to check if member needs to accept invitation
 */
export function needsAcceptance(status: MemberStatus): boolean {
  return status === MemberStatus.PENDING;
}

/**
 * Helper function to check if member has left
 */
export function hasLeft(status: MemberStatus): boolean {
  return status === MemberStatus.LEAVED;
}
