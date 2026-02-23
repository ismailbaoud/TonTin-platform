package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Notification;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.notification.NotificationStatus;
import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.notification.response.NotificationResponse;
import com.tontin.platform.repository.NotificationRepository;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.NotificationService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotifications(
        int page,
        int size,
        NotificationType type,
        Boolean isRead
    ) {
        UUID userId = securityUtils.requireCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage;
        if (isRead != null && type != null) {
            NotificationStatus status = isRead ? NotificationStatus.READ : NotificationStatus.UNREAD;
            notificationPage = notificationRepository.findByUser_IdAndStatusAndTypeOrderByCreatedAtDesc(userId, status, type, pageable);
        } else if (isRead != null) {
            NotificationStatus status = isRead ? NotificationStatus.READ : NotificationStatus.UNREAD;
            notificationPage = notificationRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else if (type != null) {
            notificationPage = notificationRepository.findByUser_IdAndTypeOrderByCreatedAtDesc(userId, type, pageable);
        } else {
            notificationPage = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId, pageable);
        }

        return toPageResponse(notificationPage, page, size);
    }

    private PageResponse<NotificationResponse> toPageResponse(Page<Notification> page, int pageNum, int size) {
        return PageResponse.<NotificationResponse>builder()
            .content(page.getContent().stream().map(this::toResponse).toList())
            .page(pageNum)
            .size(size)
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .first(page.isFirst())
            .last(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        UUID userId = securityUtils.requireCurrentUserId();
        return notificationRepository.countByUser_IdAndStatus(userId, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID notificationId) {
        UUID userId = securityUtils.requireCurrentUserId();
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your notification");
        }
        n.setStatus(NotificationStatus.READ);
        n = notificationRepository.save(n);
        return toResponse(n);
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        UUID userId = securityUtils.requireCurrentUserId();
        for (Notification n : notificationRepository.findByUser_IdAndStatusOrderByCreatedAtDesc(userId, NotificationStatus.UNREAD, Pageable.unpaged()).getContent()) {
            n.setStatus(NotificationStatus.READ);
            notificationRepository.save(n);
        }
    }

    @Override
    @Transactional
    public void delete(UUID notificationId) {
        UUID userId = securityUtils.requireCurrentUserId();
        Notification n = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your notification");
        }
        notificationRepository.delete(n);
    }

    @Override
    @Transactional
    public NotificationResponse create(
        UUID userId,
        NotificationType type,
        String title,
        String description,
        String actionUrl,
        String actionLabel
    ) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Notification n = Notification.builder()
            .user(user)
            .type(type)
            .title(title != null ? title : "")
            .description(description)
            .actionUrl(actionUrl)
            .actionLabel(actionLabel)
            .status(NotificationStatus.UNREAD)
            .build();
        n = notificationRepository.save(n);
        return toResponse(n);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
            .id(n.getId())
            .type(n.getType() != null ? n.getType().getApiValue() : "information")
            .title(n.getTitle())
            .message(n.getDescription() != null ? n.getDescription() : "")
            .isRead(n.getStatus() == NotificationStatus.READ)
            .actionUrl(n.getActionUrl())
            .actionLabel(n.getActionLabel())
            .createdDate(n.getCreatedAt())
            .build();
    }
}
