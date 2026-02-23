package com.tontin.platform.dto.dart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * Request body for creating a message in a dart's chat.
 */
@Schema(description = "Request to create a message in a dart")
public record CreateMessageRequest(
    @Size(max = 2000, message = "Message must be at most 2000 characters")
    @Schema(description = "Message text", example = "Hello everyone!")
    String content
) {}
