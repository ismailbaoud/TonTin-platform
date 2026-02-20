package com.tontin.platform.dto.round.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.Builder;

/**
 * Response for current round contribution status: which members have paid.
 */
@Builder
@Schema(description = "Contribution status for the current round - which member IDs have paid")
public record RoundContributionsResponse(
    @Schema(description = "Round id (null if no current round)")
    UUID roundId,
    @Schema(description = "Member IDs (payers) who have paid for this round")
    List<UUID> paidMemberIds
) {}
