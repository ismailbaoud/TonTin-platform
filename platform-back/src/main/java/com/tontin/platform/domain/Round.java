package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.round.RoundStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "rounds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Round extends BaseEntity {

    @NotNull(message = "Round number is required")
    @Min(value = 1, message = "Round number must be greater than or equal to 1")
    @Column(name = "round_number", nullable = false)
    private Integer number;

    @NotNull(message = "Round status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoundStatus status;

    @NotNull(message = "Round date is required")
    @Column(name = "round_date", nullable = false)
    private LocalDateTime date;

    @NotNull(message = "Round amount is required")
    @Positive(message = "Round amount must be positive")
    @Column(name = "amount", nullable = false)
    private Double amount;

    @NotNull(message = "Associated dart is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dart_id", nullable = false)
    private Dart dart;

    public boolean isPayed() {
        return RoundStatus.PAYED.equals(status);
    }

    public boolean isInPayed() {
        return RoundStatus.INPAYED.equals(status);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Round)) {
            return false;
        }
        Round round = (Round) o;
        return Objects.equals(number, round.number) &&
            Objects.equals(dart, round.dart);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, dart);
    }

    @Override
    public String toString() {
        return "Round{" +
            "id=" + getId() +
            ", number=" + number +
            ", status=" + status +
            ", date=" + date +
            ", amount=" + amount +
            ", dartId=" + (dart != null ? dart.getId() : null) +
            '}';
    }
}
