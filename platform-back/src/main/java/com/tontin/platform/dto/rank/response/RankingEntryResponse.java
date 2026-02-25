package com.tontin.platform.dto.rank.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "A single entry in the trust/points leaderboard")
public class RankingEntryResponse {

    @Schema(description = "User id", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "Display name (username)", example = "john_doe")
    private String userName;

    @Schema(description = "Total trust points", example = "150")
    private int points;

    @Schema(description = "Rank position (1-based)", example = "1")
    private int rank;

    @Schema(description = "Profile picture as base64 (JPEG/PNG)")
    private String picture;
}
