package com.tontin.platform.domain.enums.member;

/**
 * Represents the status of a member's participation in a dart.
 *
 * <ul>
 *   <li><b>PENDING</b>: Member has been invited or requested to join but not yet confirmed.</li>
 *   <li><b>ACTIVE</b>: Member is actively participating in the dart with contributions.</li>
 *   <li><b>LEAVED</b>: Member has left or been removed from the dart.</li>
 * </ul>
 */
public enum MemberStatus {
    /**
     * Member invitation is pending confirmation
     */
    PENDING,

    /**
     * Member is actively participating in the dart
     */
    ACTIVE,

    /**
     * Member has left the dart
     */
    LEAVED,
}
