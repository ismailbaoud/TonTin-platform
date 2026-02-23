package com.tontin.platform.dto.dart.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response DTO for a dart chat message.
 */
@Builder
@Schema(description = "A message in a dart's chat")
public record MessageResponse(
    @Schema(description = "Message unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "Dart unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID dartId,

    @Schema(description = "User ID of the sender", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID userId,

    @Schema(description = "Display name of the sender", example = "John Doe")
    String userName,

    @Schema(description = "Message content", example = "Hello everyone!")
    String content,

    @Schema(description = "When the message was sent", example = "2024-01-15T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "Reactions on this message (emoji, count, whether current user reacted)")
    List<ReactionSummary> reactions
) {
    @Builder
    @Schema(description = "Summary of one reaction type on a message")
    public record ReactionSummary(
        @Schema(description = "Emoji character(s)", example = "👍") String emoji,
        @Schema(description = "Number of users who used this reaction") long count,
        @Schema(description = "Whether the current user has this reaction") boolean reactedByCurrentUser,
        @Schema(description = "Display names of users who reacted (for hover tooltip)") List<String> reactedByUserNames
    ) {}
}
