package com.tontin.platform.config;

import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserStatus;
import java.util.Collection;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Custom implementation of Spring Security's UserDetails interface.
 *
 * <p>This class wraps the application's User entity and provides the necessary
 * information for Spring Security authentication and authorization. It implements
 * all account status checks and provides convenient access to user information.</p>
 *
 * <p>The class is immutable and thread-safe, making it suitable for storing in
 * the SecurityContext across multiple requests.</p>
 */
@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /**
     * The unique identifier of the user.
     */
    private final UUID id;

    /**
     * The username (email) used for authentication.
     */
    private final String username;

    /**
     * The encoded password hash.
     */
    private final String password;

    /**
     * The full User entity for accessing additional user information.
     */
    private final User user;

    /**
     * The collection of granted authorities (roles and permissions).
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Indicates whether the user's account has expired.
     *
     * <p>An expired account cannot be authenticated. In this implementation,
     * accounts never expire.</p>
     *
     * @return true always, as accounts do not expire
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked or unlocked.
     *
     * <p>A locked user cannot be authenticated. In this implementation,
     * we check if the account is suspended or disabled.</p>
     *
     * @return true if the account is not suspended or disabled
     */
    @Override
    public boolean isAccountNonLocked() {
        return (
            user.getStatus() != UserStatus.SUSPENDED &&
            user.getStatus() != UserStatus.DISABLED
        );
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     *
     * <p>Expired credentials prevent authentication. In this implementation,
     * credentials never expire.</p>
     *
     * @return true always, as credentials do not expire
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     *
     * <p>A disabled user cannot be authenticated. In this implementation,
     * a user is enabled only if their status is ACTIVE.</p>
     *
     * @return true if the user status is ACTIVE
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == UserStatus.ACTIVE;
    }

    /**
     * Gets the user's email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return user.getEmail();
    }

    @Override
    public String toString() {
        return (
            "CustomUserDetails{" +
            "id=" +
            id +
            ", username='" +
            username +
            '\'' +
            ", authorities=" +
            authorities +
            ", status=" +
            user.getStatus() +
            ", enabled=" +
            isEnabled() +
            '}'
        );
    }
}
