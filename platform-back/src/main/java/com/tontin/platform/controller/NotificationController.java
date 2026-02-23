package com.tontin.platform.controller;

import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.notification.response.NotificationResponse;
import com.tontin.platform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Notifications", description = "User notifications (payments, invites, reminders)")
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "List notifications", description = "Paginated list of current user's notifications. Optional filters: type, isRead.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paginated notifications", content = @Content(schema = @Schema(implementation = PageResponse.class))),
    })
    public ResponseEntity<PageResponse<NotificationResponse>> getNotifications(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Filter by type (e.g. payment_due)") @RequestParam(required = false) String type,
        @Parameter(description = "Filter by read status") @RequestParam(required = false) Boolean isRead
    ) {
        NotificationType typeEnum = type != null && !type.isBlank() ? NotificationType.fromApiValue(type.trim()) : null;
        PageResponse<NotificationResponse> body = notificationService.getNotifications(page, size, typeEnum, isRead);
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/unread-count", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Unread count", description = "Number of unread notifications for the current user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Unread count"),
    })
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PutMapping(value = "/{id}/read", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Mark as read", description = "Mark a notification as read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Updated notification"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
    })
    public ResponseEntity<NotificationResponse> markAsRead(
        @Parameter(description = "Notification id", required = true) @PathVariable("id") UUID id
    ) {
        NotificationResponse body = notificationService.markAsRead(id);
        return ResponseEntity.ok(body);
    }

    @PutMapping(value = "/mark-all-read", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Mark all as read", description = "Mark all notifications of the current user as read.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
    })
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Delete notification", description = "Delete a notification. Only the owner can delete.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "404", description = "Notification not found"),
    })
    public ResponseEntity<Void> delete(
        @Parameter(description = "Notification id", required = true) @PathVariable("id") UUID id
    ) {
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
