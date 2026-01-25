package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.logs.LogsLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "logs",
    indexes = {
        @Index(name = "idx_logs_timestamp", columnList = "timestamp"),
        @Index(name = "idx_logs_user_email", columnList = "user_email"),
        @Index(name = "idx_logs_event", columnList = "event"),
        @Index(name = "idx_logs_status", columnList = "status"),
        @Index(name = "idx_logs_request_id", columnList = "request_id"),
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loggin {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @NotNull(message = "Timestamp is required")
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 20)
    private LogsLevel level;

    @NotBlank(message = "Service is required")
    @Column(name = "service", nullable = false, length = 100)
    private String service;

    @NotBlank(message = "Environment is required")
    @Column(name = "env", nullable = false, length = 20)
    private String env;

    @NotBlank(message = "Request ID is required")
    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;

    @NotBlank(message = "Event is required")
    @Column(name = "event", nullable = false, length = 100)
    private String event;

    @NotBlank(message = "Path is required")
    @Column(name = "path", nullable = false, length = 255)
    private String path;

    @NotBlank(message = "Status is required")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    // Business key equals/hashCode based on requestId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Loggin loggin = (Loggin) o;
        return requestId != null && requestId.equals(loggin.requestId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestId);
    }

    @Override
    public String toString() {
        return (
            "Loggin{" +
            "id=" +
            id +
            ", timestamp=" +
            timestamp +
            ", level=" +
            level +
            ", service='" +
            service +
            '\'' +
            ", env='" +
            env +
            '\'' +
            ", requestId='" +
            requestId +
            '\'' +
            ", event='" +
            event +
            '\'' +
            ", status='" +
            status +
            '\'' +
            ", userEmail='" +
            userEmail +
            '\'' +
            '}'
        );
    }

    // Business methods
    public boolean isSuccess() {
        return "SUCCESS".equalsIgnoreCase(status);
    }

    public boolean isFailure() {
        return "FAILURE".equalsIgnoreCase(status);
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.isEmpty();
    }
}
