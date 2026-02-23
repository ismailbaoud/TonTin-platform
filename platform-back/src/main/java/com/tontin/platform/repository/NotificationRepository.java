package com.tontin.platform.repository;

import com.tontin.platform.domain.Notification;
import com.tontin.platform.domain.enums.notification.NotificationStatus;
import com.tontin.platform.domain.enums.notification.NotificationType;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    @EntityGraph(attributePaths = {"user"})
    Page<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Notification> findByUser_IdAndStatusOrderByCreatedAtDesc(UUID userId, NotificationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Notification> findByUser_IdAndTypeOrderByCreatedAtDesc(UUID userId, NotificationType type, Pageable pageable);

    @EntityGraph(attributePaths = {"user"})
    Page<Notification> findByUser_IdAndStatusAndTypeOrderByCreatedAtDesc(UUID userId, NotificationStatus status, NotificationType type, Pageable pageable);

    long countByUser_IdAndStatus(UUID userId, NotificationStatus status);
}
