package com.tontin.platform.Exception;

import com.tontin.platform.dto.auth.exception.ApiExceptionResponse;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiExceptionResponse> handleResponseStatusException(
        ResponseStatusException ex,
        HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return buildErrorResponse(
            status,
            ex.getReason() != null ? ex.getReason() : ex.getMessage(),
            request,
            status.is4xxClientError() ? "CLIENT_ERROR" : "SERVER_ERROR",
            null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<
        ApiExceptionResponse
    > handleMethodArgumentNotValidException(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ApiExceptionResponse.FieldError> fieldErrors = ex
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());

        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed for the request payload",
            request,
            "VALIDATION_ERROR",
            fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<
        ApiExceptionResponse
    > handleConstraintViolationException(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<ApiExceptionResponse.FieldError> fieldErrors = ex
            .getConstraintViolations()
            .stream()
            .map(this::toFieldError)
            .collect(Collectors.toList());

        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed for the request parameters",
            request,
            "VALIDATION_ERROR",
            fieldErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiExceptionResponse> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        String param = ex.getName();
        Object value = ex.getValue();
        String requiredType = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
        String message = String.format(
            "Invalid value for parameter '%s': '%s' is not a valid %s. Use a valid identifier (e.g. UUID).",
            param,
            value,
            requiredType
        );
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            message,
            request,
            "CLIENT_ERROR",
            null
        );
    }

    @ExceptionHandler(
        { IllegalArgumentException.class, IllegalStateException.class }
    )
    public ResponseEntity<ApiExceptionResponse> handleIllegalArgumentExceptions(
        RuntimeException ex,
        HttpServletRequest request
    ) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            request,
            "CLIENT_ERROR",
            null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiExceptionResponse> handleDataIntegrityViolation(
        DataIntegrityViolationException ex,
        HttpServletRequest request
    ) {
        String message = ex.getMessage();
        String detail;
        if (message != null && (
            message.contains("users_user_name_key") ||
            message.contains("user_name") && message.contains("unique") ||
            message.contains("username") && message.contains("unique")
        )) {
            detail = "Username already taken.";
        } else if (message != null && (
            message.contains("users_email_key") ||
            message.contains("email") && message.contains("unique")
        )) {
            detail = "An account with this email already exists.";
        } else if (message != null && message.contains("duplicate key")) {
            detail = "An account with this email or username already exists.";
        } else {
            detail = "A database constraint was violated. Please check your data.";
        }
        log.warn("Data integrity violation on [{}]: {}", request.getRequestURI(), message);
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            detail,
            request,
            "CONFLICT",
            null
        );
    }

    @ExceptionHandler({
        MailAuthenticationException.class,
        MailSendException.class,
        MessagingException.class,
    })
    public ResponseEntity<ApiExceptionResponse> handleMailExceptions(
        Exception ex,
        HttpServletRequest request
    ) {
        log.warn(
            "Mail error on [{}]: {}",
            request.getRequestURI(),
            ex.getMessage()
        );
        return buildErrorResponse(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Verification email could not be sent. Your account was created; you can try logging in or contact support to verify your email.",
            request,
            "MAIL_ERROR",
            null
        );
    }

    @ExceptionHandler({ SecurityException.class, AccessDeniedException.class })
    public ResponseEntity<ApiExceptionResponse> handleSecurityExceptions(
        Exception ex,
        HttpServletRequest request
    ) {
        HttpStatus status =
            ex instanceof AccessDeniedException
                ? HttpStatus.FORBIDDEN
                : HttpStatus.UNAUTHORIZED;

        return buildErrorResponse(
            status,
            ex.getMessage(),
            request,
            "SECURITY_ERROR",
            null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionResponse> handleUnhandledExceptions(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error(
            "Unhandled exception processing [{}]: {}",
            request.getRequestURI(),
            ex.getMessage(),
            ex
        );
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later.",
            request,
            "INTERNAL_SERVER_ERROR",
            null
        );
    }

    private ResponseEntity<ApiExceptionResponse> buildErrorResponse(
        HttpStatus status,
        String detail,
        HttpServletRequest request,
        String errorCode,
        List<ApiExceptionResponse.FieldError> fieldErrors
    ) {
        ApiExceptionResponse response = ApiExceptionResponse.builder()
            .status(status.value())
            .detail(detail)
            .path(request.getRequestURI())
            .timestamp(Instant.now())
            .errorCode(errorCode)
            .traceId(UUID.randomUUID())
            .fieldErrors(fieldErrors)
            .build();

        return ResponseEntity.status(status).body(response);
    }

    private ApiExceptionResponse.FieldError toFieldError(FieldError error) {
        return ApiExceptionResponse.FieldError.builder()
            .field(error.getField())
            .rejectedValue(error.getRejectedValue())
            .message(error.getDefaultMessage())
            .build();
    }

    private ApiExceptionResponse.FieldError toFieldError(
        ConstraintViolation<?> violation
    ) {
        return ApiExceptionResponse.FieldError.builder()
            .field(violation.getPropertyPath().toString())
            .rejectedValue(violation.getInvalidValue())
            .message(violation.getMessage())
            .build();
    }
}
