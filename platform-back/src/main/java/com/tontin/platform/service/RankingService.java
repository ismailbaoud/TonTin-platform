package com.tontin.platform.service;

import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.rank.response.RankingEntryResponse;
import java.util.UUID;

public interface RankingService {

    /**
     * Get leaderboard: users ordered by points descending, with rank.
     *
     * @param page     page number (0-based)
     * @param size     page size
     * @return paginated ranking entries
     */
    PageResponse<RankingEntryResponse> getRankings(int page, int size);

    /**
     * Get current user's rank and points (or null if not found).
     */
    RankingEntryResponse getCurrentUserRanking();
}
