package com.tontin.platform.domain.enums.user;

/**
 * Represents the account status of a user in the system.
 *
 * <ul>
 *   <li><b>PENDING</b>: User account is awaiting email verification.</li>
 *   <li><b>ACTIVE</b>: User account is verified and active, can access the platform.</li>
 *   <li><b>SUSPENDED</b>: User account is temporarily suspended due to policy violations or security concerns.</li>
 *   <li><b>DISABLED</b>: User account has been disabled by an administrator.</li>
 *   <li><b>DELETED</b>: User account has been marked for deletion (soft delete).</li>
 * </ul>
 */
public enum UserStatus {
    /**
     * Account is pending email verification
     */
    PENDING,

    /**
     * Account is verified and active
     */
    ACTIVE,

    /**
     * Account is temporarily suspended
     */
    SUSPENDED,

    /**
     * Account has been disabled by admin
     */
    DISABLED,

    /**
     * Account has been marked for deletion
     */
    DELETED,
}
