package com.tontin.platform.dto.notification.response;

import com.tontin.platform.domain.enums.notification.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Notification item for the current user")
public class NotificationResponse {

    @Schema(description = "Notification id", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Notification type", example = "payment_due")
    private String type;

    @Schema(description = "Title", example = "Contribution due")
    private String title;

    @Schema(description = "Message/body", example = "Your monthly payment is due tomorrow.")
    private String message;

    @Schema(description = "Whether the notification has been read")
    private boolean isRead;

    @Schema(description = "Optional URL for action button", example = "/dashboard/client/pay-contribution/1")
    private String actionUrl;

    @Schema(description = "Optional label for action button", example = "Pay Now")
    private String actionLabel;

    @Schema(description = "When the notification was created", example = "2024-01-15T10:30:00")
    private LocalDateTime createdDate;
}
