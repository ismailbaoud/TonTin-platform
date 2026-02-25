package com.tontin.platform.service;

import com.tontin.platform.domain.enums.rank.PointAction;
import java.util.UUID;

/**
 * Service to add or deduct trust points for users. Updates user total and writes rank history.
 */
public interface PointsService {

    /**
     * Apply a point action for a user. Updates user.points and creates a RankHistory record.
     *
     * @param userId   the user to award or penalize
     * @param action   the action (defines points delta and description)
     * @param dartId   optional dart context (can be null)
     * @return new total points for the user, or null if user not found / action skipped
     */
    Integer addPoints(UUID userId, PointAction action, UUID dartId);
}
