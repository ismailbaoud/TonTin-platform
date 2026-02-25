package com.tontin.platform.controller;

import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.rank.response.RankingEntryResponse;
import com.tontin.platform.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
@Tag(name = "Rankings", description = "Trust points leaderboard")
@SecurityRequirement(name = "Bearer Authentication")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Get leaderboard", description = "Paginated list of users ordered by trust points (desc).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Leaderboard page", content = @Content(schema = @Schema(implementation = PageResponse.class))),
    })
    public ResponseEntity<PageResponse<RankingEntryResponse>> getRankings(
        @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<RankingEntryResponse> body = rankingService.getRankings(page, size);
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('CLIENT', 'ADMIN')")
    @Operation(summary = "Get my ranking", description = "Current user's rank and points.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current user ranking"),
    })
    public ResponseEntity<RankingEntryResponse> getMyRanking() {
        RankingEntryResponse body = rankingService.getCurrentUserRanking();
        return body != null ? ResponseEntity.ok(body) : ResponseEntity.notFound().build();
    }
}
