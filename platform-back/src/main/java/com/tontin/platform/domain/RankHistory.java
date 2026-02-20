package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.rank.PointsType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * History of rank/trust points changes for a user (e.g. penalty for late payment, prize).
 * Optionally linked to a dart when points are per-dart.
 */
@Entity
@Table(name = "rank_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RankHistory extends BaseEntity {

    @NotNull(message = "Points is required")
    @Column(name = "points", nullable = false)
    private Integer points;

    @NotNull(message = "Points type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PointsType type;

    @NotNull(message = "Date is required")
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime date;

    @Column(name = "action", length = 255)
    private String action;

    @NotNull(message = "User is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Optional: when points are scoped to a specific dart. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dart_id")
    private Dart dart;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RankHistory that = (RankHistory) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "RankHistory{" +
            "id=" + getId() +
            ", points=" + points +
            ", type=" + type +
            ", date=" + date +
            ", action='" + action + '\'' +
            ", userId=" + (user != null ? user.getId() : null) +
            ", dartId=" + (dart != null ? dart.getId() : null) +
            '}';
    }
}
