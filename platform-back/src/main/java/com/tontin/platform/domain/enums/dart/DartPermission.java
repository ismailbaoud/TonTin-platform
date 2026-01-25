package com.tontin.platform.domain.enums.dart;

/**
 * Represents the permission levels a member can have within a dart.
 *
 * <ul>
 *   <li><b>ORGANIZER</b>: Has full control over the dart, can manage members,
 *       modify dart settings, and perform administrative actions.</li>
 *   <li><b>MEMBER</b>: Regular participant in the dart with standard access rights.</li>
 * </ul>
 */
public enum DartPermission {
    /**
     * Organizer with full administrative privileges
     */
    ORGANIZER,

    /**
     * Regular member with standard access
     */
    MEMBER,
}
