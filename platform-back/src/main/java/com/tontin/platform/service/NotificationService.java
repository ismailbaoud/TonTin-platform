package com.tontin.platform.service;

import com.tontin.platform.domain.enums.notification.NotificationStatus;
import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.notification.response.NotificationResponse;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * Get paginated notifications for the current user.
     *
     * @param page   page number (0-based)
     * @param size   page size
     * @param type   optional filter by type
     * @param isRead optional filter by read status (true = read, false = unread)
     * @return paginated notifications
     */
    PageResponse<NotificationResponse> getNotifications(
        int page,
        int size,
        NotificationType type,
        Boolean isRead
    );

    /**
     * Get unread count for the current user.
     */
    long getUnreadCount();

    /**
     * Mark a notification as read. Only the owner can do this.
     *
     * @param notificationId notification id
     * @return updated notification response
     */
    NotificationResponse markAsRead(UUID notificationId);

    /**
     * Mark all notifications of the current user as read.
     */
    void markAllAsRead();

    /**
     * Delete a notification. Only the owner can do this.
     *
     * @param notificationId notification id
     */
    void delete(UUID notificationId);

    /**
     * Create a notification for a user (e.g. from payment reminder, dar invite). Used by other services.
     *
     * @param userId      target user id
     * @param type        notification type
     * @param title       title
     * @param description message body
     * @param actionUrl   optional action URL
     * @param actionLabel optional action button label
     * @return created notification response
     */
    NotificationResponse create(
        UUID userId,
        NotificationType type,
        String title,
        String description,
        String actionUrl,
        String actionLabel
    );
}
