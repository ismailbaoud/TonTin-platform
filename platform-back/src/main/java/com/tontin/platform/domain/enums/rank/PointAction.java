package com.tontin.platform.domain.enums.rank;

import lombok.Getter;

/**
 * Actions that add or deduct trust points. Used for dynamic ranking.
 * Positive points = reward, negative = penalty.
 */
@Getter
public enum PointAction {

    // Rewards (positive points)
    DAR_CREATED(15, "Created a Dâr"),
    DAR_STARTED(10, "Started a Dâr"),
    INVITATION_ACCEPTED(10, "Accepted an invitation to join a Dâr"),
    PAYMENT_ON_TIME(20, "Paid contribution on time"),
    PAYOUT_RECEIVED(5, "Received round payout"),
    MEMBER_JOINED_YOUR_DAR(5, "Someone accepted your invitation"),

    // Penalties (negative points)
    PAYMENT_LATE(-25, "Late payment"),
    INVITATION_REJECTED(-5, "Declined an invitation"),
    LEFT_DAR(-10, "Left a Dâr"),
    REMOVED_FROM_DAR(-15, "Removed from a Dâr");

    private final int points;
    private final String description;

    PointAction(int points, String description) {
        this.points = points;
        this.description = description;
    }

    public boolean isReward() {
        return points > 0;
    }

    public PointsType toPointsType() {
        return points >= 0 ? PointsType.PRIZE : PointsType.PENALTY;
    }
}
