package com.tontin.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tontin.platform.dto.auth.exception.ApiExceptionResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * JWT Authentication Entry Point.
 *
 * <p>This component handles authentication failures and unauthorized access attempts.
 * It sends a standardized JSON error response when authentication fails.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
        HttpServletRequest request,
        HttpServletResponse response,
        AuthenticationException authException
    ) throws IOException, ServletException {

        log.warn(
            "Unauthorized access attempt to: {} - Reason: {}",
            request.getRequestURI(),
            authException.getMessage()
        );

        // Set response status and content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build error response
        ApiExceptionResponse errorResponse = ApiExceptionResponse.builder()
            .status(HttpServletResponse.SC_UNAUTHORIZED)
            .detail(determineErrorMessage(authException, request))
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .errorCode("AUTHENTICATION_FAILED")
            .traceId(UUID.randomUUID())
            .build();

        // Write JSON response
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }

    /**
     * Determines the appropriate error message based on the authentication exception.
     *
     * @param authException the authentication exception
     * @param request the HTTP request
     * @return the error message to send to the client
     */
    private String determineErrorMessage(
        AuthenticationException authException,
        HttpServletRequest request
    ) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || authHeader.isEmpty()) {
            return "Authentication credentials are missing. Please provide a valid JWT token.";
        }

        if (!authHeader.startsWith("Bearer ")) {
            return "Invalid authorization header format. Use 'Bearer <token>'.";
        }

        // Check if it's an expired token
        if (authException.getMessage() != null &&
            authException.getMessage().toLowerCase().contains("expired")) {
            return "JWT token has expired. Please refresh your token or login again.";
        }

        // Generic authentication failure message
        return "Authentication failed. Please check your credentials and try again.";
    }
}
