package com.tontin.platform.repository;

import com.tontin.platform.domain.Notification;
import com.tontin.platform.domain.enums.notification.NotificationStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // List<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    // Page<Notification> findAllByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    long countByUserIdAndStatus(UUID userId, NotificationStatus status);
}
