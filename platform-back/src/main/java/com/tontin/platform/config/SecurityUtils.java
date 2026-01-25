package com.tontin.platform.config;

import com.tontin.platform.domain.User;
import com.tontin.platform.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final UserRepository userRepository;

    private Optional<Authentication> getCurrentAuthentication() {
        return Optional.ofNullable(
            SecurityContextHolder.getContext().getAuthentication()
        );
    }

    private Optional<CustomUserDetails> getCurrentUserDetails() {
        return getCurrentAuthentication()
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .filter(CustomUserDetails.class::isInstance)
            .map(CustomUserDetails.class::cast);
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentUserDetails().map(CustomUserDetails::getEmail);
    }

    public UUID requireCurrentUserId() {
        return getCurrentUserDetails()
            .map(CustomUserDetails::getId)
            .orElseThrow(() -> {
                log.error(
                    "Attempted to access protected resource without authentication"
                );
                return new SecurityException("User must be authenticated");
            });
    }

    public User requireCurrentUser() {
        UUID userId = requireCurrentUserId();
        return userRepository
            .findById(userId)
            .orElseThrow(() -> {
                log.error(
                    "Authenticated user not found in repository: {}",
                    userId
                );
                return new SecurityException("User must be authenticated");
            });
    }

    public void clearSecurityContext() {
        log.info(
            "Clearing security context for user: {}",
            getCurrentUserDetails()
                .map(CustomUserDetails::getEmail)
                .orElse("unknown")
        );
        SecurityContextHolder.clearContext();
    }
}
