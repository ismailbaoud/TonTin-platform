package com.tontin.platform.Exception;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tontin.platform.dto.auth.exception.ApiExceptionResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
    


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiExceptionResponse> globalExceptionHandler(Exception ex, HttpServletRequest request) {
        return buildError(ex.getMessage(), request, HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiExceptionResponse> buildError(String message , HttpServletRequest request, HttpStatus status) {
        ApiExceptionResponse response = ApiExceptionResponse.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail(message)
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .traceId(UUID.randomUUID())
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
