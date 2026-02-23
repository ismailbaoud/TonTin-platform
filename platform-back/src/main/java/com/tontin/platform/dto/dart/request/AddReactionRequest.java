package com.tontin.platform.dto.dart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to add a reaction (emoji) to a message")
public record AddReactionRequest(
    @NotBlank(message = "Emoji is required")
    @Size(max = 20, message = "Emoji must be at most 20 characters")
    @Schema(description = "Emoji character(s)", example = "👍", requiredMode = Schema.RequiredMode.REQUIRED)
    String emoji
) {}
