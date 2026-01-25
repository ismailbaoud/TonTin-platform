package com.tontin.platform.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT Authentication Filter.
 *
 * <p>This filter intercepts incoming HTTP requests and validates JWT tokens
 * from the Authorization header. If a valid token is found, it sets the
 * authentication in the Spring Security context.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        // Skip filter if no Authorization header or doesn't start with "Bearer "
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT token
            final String jwt = authHeader.substring(BEARER_PREFIX_LENGTH);
            final String userEmail = jwtService.extractUsername(jwt);

            // Check if user email is present and no authentication exists in context
            if (
                userEmail != null &&
                SecurityContextHolder.getContext().getAuthentication() == null
            ) {
                // Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(
                    userEmail
                );

                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                        );

                    // Set additional details
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(
                            request
                        )
                    );

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(
                        authToken
                    );

                    log.debug(
                        "JWT authentication successful for user: {}",
                        userEmail
                    );
                } else {
                    log.warn("Invalid JWT token for user: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("JWT authentication failed: {}", e.getMessage());
            // Don't block the filter chain - let Spring Security handle unauthorized access
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Skip JWT validation for public endpoints only
        // Note: /api/v1/auth/me and /api/v1/auth/logout require authentication
        String path = request.getServletPath();
        return (
            path.equals("/api/v1/auth/login") ||
            path.equals("/api/v1/auth/register") ||
            path.startsWith("/api/v1/auth/verify") ||
            path.equals("/api/v1/auth/refresh-token") ||
            path.startsWith("/swagger-ui") ||
            path.startsWith("/v3/api-docs") ||
            path.startsWith("/actuator")
        );
    }
}
