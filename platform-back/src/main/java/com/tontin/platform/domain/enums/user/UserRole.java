package com.tontin.platform.domain.enums.user;

/**
 * Represents the authorization roles available in the platform.
 *
 * <ul>
 *   <li><b>ROLE_ADMIN</b>: Administrator with full system access and management capabilities.</li>
 *   <li><b>ROLE_CLIENT</b>: Regular user/client with standard platform access.</li>
 * </ul>
 */
public enum UserRole {
    /**
     * Administrator role with full system privileges
     */
    ROLE_ADMIN,

    /**
     * Standard client/user role with regular access
     */
    ROLE_CLIENT,
}
