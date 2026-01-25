package com.tontin.platform.dto.auth.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for API exceptions and error responses.
 *
 * <p>This immutable record provides a standardized structure for error responses
 * across the API, including HTTP status, error details, validation errors, and
 * tracing information for debugging.</p>
 *
 * @param status      HTTP status code
 * @param detail      Detailed error message
 * @param path        Request path where the error occurred
 * @param timestamp   Time when the error occurred
 * @param errorCode   Application-specific error code (optional)
 * @param traceId     Unique identifier for tracing the error
 * @param fieldErrors List of field-specific validation errors (optional)
 */
@Builder
@Schema(description = "Standardized error response object")
public record ApiExceptionResponse(
    @Schema(
        description = "HTTP status code",
        example = "400",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    int status,

    @Schema(
        description = "Detailed error message",
        example = "Invalid request parameters",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String detail,

    @Schema(
        description = "Request path where the error occurred",
        example = "/api/v1/dart",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String path,

    @Schema(
        description = "Timestamp when the error occurred",
        example = "2024-01-15T10:30:00Z",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    Instant timestamp,

    @Schema(
        description = "Application-specific error code for client-side error handling",
        example = "VALIDATION_ERROR"
    )
    String errorCode,

    @Schema(
        description = "Unique identifier for tracing the error in logs",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID traceId,

    @Schema(description = "List of field-specific validation errors")
    List<FieldError> fieldErrors
) {
    /**
     * Nested record for field-specific validation errors.
     *
     * @param field         Name of the field that failed validation
     * @param rejectedValue The value that was rejected
     * @param message       Error message describing why the field is invalid
     */
    @Builder
    @Schema(description = "Field-specific validation error")
    public record FieldError(
        @Schema(
            description = "Name of the field that failed validation",
            example = "email"
        )
        String field,
        @Schema(
            description = "The value that was rejected",
            example = "invalid-email"
        )
        Object rejectedValue,
        @Schema(
            description = "Error message describing the validation failure",
            example = "Email must be valid"
        )
        String message
    ) {
        /**
         * Checks if the field error has all required information.
         *
         * @return true if field and message are not null
         */
        public boolean isValid() {
            return field != null && message != null;
        }
    }

    /**
     * Checks if this is a validation error (has field errors).
     *
     * @return true if there are field-specific validation errors
     */
    public boolean hasFieldErrors() {
        return fieldErrors != null && !fieldErrors.isEmpty();
    }

    /**
     * Checks if this is a client error (4xx status code).
     *
     * @return true if status is between 400 and 499
     */
    public boolean isClientError() {
        return status >= 400 && status < 500;
    }

    /**
     * Checks if this is a server error (5xx status code).
     *
     * @return true if status is between 500 and 599
     */
    public boolean isServerError() {
        return status >= 500 && status < 600;
    }

    /**
     * Gets the total number of field errors.
     *
     * @return count of field errors, or 0 if none
     */
    public int getFieldErrorCount() {
        return fieldErrors != null ? fieldErrors.size() : 0;
    }

    /**
     * Checks if a specific field has an error.
     *
     * @param fieldName the field name to check
     * @return true if the specified field has an error
     */
    public boolean hasErrorForField(String fieldName) {
        if (fieldErrors == null || fieldName == null) {
            return false;
        }
        return fieldErrors
            .stream()
            .anyMatch(error -> fieldName.equals(error.field()));
    }

    /**
     * Gets the error message for a specific field.
     *
     * @param fieldName the field name to get the error message for
     * @return the error message, or null if no error for that field
     */
    public String getFieldErrorMessage(String fieldName) {
        if (fieldErrors == null || fieldName == null) {
            return null;
        }
        return fieldErrors
            .stream()
            .filter(error -> fieldName.equals(error.field()))
            .map(FieldError::message)
            .findFirst()
            .orElse(null);
    }
}
