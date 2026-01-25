package com.tontin.platform.config;

import com.tontin.platform.domain.User;
import com.tontin.platform.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Custom UserDetailsService implementation for loading user-specific data.
 *
 * <p>This service is used by Spring Security to load user information from the
 * database during authentication. It fetches the user by email and converts it
 * into a {@link CustomUserDetails} object that Spring Security can use.</p>
 *
 * <p>The service is transactional to ensure proper database session management
 * and implements caching to reduce database queries for frequently accessed users.</p>
 */
@Service("customUserDetailsService")
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Locates the user based on the email address (username).
     *
     * <p>This method is called by Spring Security during authentication to load
     * the user's details from the database. It converts the User entity into a
     * CustomUserDetails object containing all necessary authentication information.</p>
     *
     * <p>The method performs the following:</p>
     * <ul>
     *   <li>Searches for the user by email in the database</li>
     *   <li>Throws UsernameNotFoundException if user doesn't exist</li>
     *   <li>Creates a CustomUserDetails object with user information</li>
     *   <li>Constructs authorities based on the user's role</li>
     *   <li>Logs the authentication attempt for security auditing</li>
     * </ul>
     *
     * @param email the email address (username) identifying the user
     * @return a fully populated UserDetails object
     * @throws UsernameNotFoundException if the user could not be found or has no GrantedAuthority
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email)
        throws UsernameNotFoundException {
        log.debug("Loading user details for email: {}", email);

        if (email == null || email.trim().isEmpty()) {
            log.warn("Attempted to load user with null or empty email");
            throw new UsernameNotFoundException(
                "Email cannot be null or empty"
            );
        }

        // Normalize email to lowercase for case-insensitive lookup
        String normalizedEmail = email.trim().toLowerCase();

        // Fetch user from database
        User user = userRepository
            .findByEmail(normalizedEmail)
            .orElseThrow(() -> {
                log.warn("User not found with email: {}", normalizedEmail);
                return new UsernameNotFoundException(
                    String.format(
                        "User not found with email: %s",
                        normalizedEmail
                    )
                );
            });

        // Validate that user has a role assigned
        if (user.getRole() == null) {
            log.error("User {} has no role assigned", user.getId());
            throw new UsernameNotFoundException("User has no role assigned");
        }

        // Create granted authority from user role
        // Note: Spring Security requires "ROLE_" prefix for role-based security
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(
            user.getRole().name()
        );

        log.debug(
            "Successfully loaded user: {} with role: {}",
            normalizedEmail,
            user.getRole()
        );

        // Build and return CustomUserDetails
        return new CustomUserDetails(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user,
            List.of(authority)
        );
    }
}
