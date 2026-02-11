/**
 * Member role enumeration for Dâr membership
 *
 * Defines the different roles a member can have within a Dâr.
 */
export enum MemberRole {
  /** Dâr organizer with full administrative privileges */
  ORGANIZER = 'organizer',

  /** Regular member with standard privileges */
  MEMBER = 'member',

  /** Co-organizer with some administrative privileges */
  CO_ORGANIZER = 'co-organizer'
}

/**
 * Helper function to get user-friendly role label
 */
export function getMemberRoleLabel(role: MemberRole): string {
  const labels: Record<MemberRole, string> = {
    [MemberRole.ORGANIZER]: 'Organizer',
    [MemberRole.MEMBER]: 'Member',
    [MemberRole.CO_ORGANIZER]: 'Co-Organizer'
  };
  return labels[role];
}

/**
 * Helper function to get role badge color class for UI
 */
export function getMemberRoleBadgeColor(role: MemberRole): string {
  const colors: Record<MemberRole, string> = {
    [MemberRole.ORGANIZER]: 'bg-primary/20 text-primary',
    [MemberRole.MEMBER]: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
    [MemberRole.CO_ORGANIZER]: 'bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300'
  };
  return colors[role];
}

/**
 * Helper function to check if role has administrative privileges
 */
export function hasAdminPrivileges(role: MemberRole): boolean {
  return role === MemberRole.ORGANIZER || role === MemberRole.CO_ORGANIZER;
}

/**
 * Helper function to check if role can manage members
 */
export function canManageMembers(role: MemberRole): boolean {
  return role === MemberRole.ORGANIZER;
}

/**
 * Helper function to check if role can edit Dâr settings
 */
export function canEditDarSettings(role: MemberRole): boolean {
  return role === MemberRole.ORGANIZER;
}

/**
 * Helper function to check if role can delete Dâr
 */
export function canDeleteDar(role: MemberRole): boolean {
  return role === MemberRole.ORGANIZER;
}

/**
 * Helper function to check if role can invite members
 */
export function canInviteMembers(role: MemberRole): boolean {
  return hasAdminPrivileges(role);
}

/**
 * Helper function to check if role can send messages
 */
export function canSendMessages(role: MemberRole): boolean {
  return true; // All members can send messages
}

/**
 * Helper function to get role permissions summary
 */
export function getRolePermissions(role: MemberRole): {
  canManageMembers: boolean;
  canEditSettings: boolean;
  canDeleteDar: boolean;
  canInviteMembers: boolean;
  canSendMessages: boolean;
} {
  return {
    canManageMembers: canManageMembers(role),
    canEditSettings: canEditDarSettings(role),
    canDeleteDar: canDeleteDar(role),
    canInviteMembers: canInviteMembers(role),
    canSendMessages: canSendMessages(role)
  };
}
