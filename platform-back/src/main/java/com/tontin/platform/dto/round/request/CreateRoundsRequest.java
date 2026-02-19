package com.tontin.platform.dto.round.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for creating rounds when a dart starts.
 *
 * <p>
 * This immutable record is used to trigger the creation of rounds
 * for a dart based on its order method and member count.
 * </p>
 *
 * @param dartId The ID of the dart for which rounds should be created
 */
@Schema(description = "Request object for creating rounds when a dart starts")
public record CreateRoundsRequest(
    @NotNull(message = "Dart ID is required")
    @Schema(
        description = "Unique identifier of the dart",
        example = "123e4567-e89b-12d3-a456-426614174000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    UUID dartId
) {
}
