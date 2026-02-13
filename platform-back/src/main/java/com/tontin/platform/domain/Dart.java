package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.round.OrderMethod;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "darts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dart extends BaseEntity {

    @NotBlank(message = "Dart name is required")
    @Size(min = 3, max = 100, message = "Dart name must be between 3 and 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "start_date", nullable = true)
    private LocalDateTime startDate;

    @NotNull(message = "Monthly contribution is required")
    @DecimalMin(value = "0.01", message = "Monthly contribution must be greater than zero")
    @Column(name = "monthly_contribution", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyContribution;

    @NotNull(message = "Order Method is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "order_method", nullable = false, length = 50)
    private OrderMethod orderMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private DartStatus status = DartStatus.PENDING;

    @Column(name = "custom_rules", nullable = true)
    private String customRules;

    @Column(name = "description", length = 500)
    private String description;

    @NotBlank(message = "Payment Frequency is required")
    @Column(name = "payment_frequency", nullable = false, length = 50)
    private String paymentFrequency;

    @OneToMany(mappedBy = "dart", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Member> members = new ArrayList<>();

    // Business key equals/hashCode based on name and startDate
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Dart dart = (Dart) o;
        return getId() != null && getId().equals(dart.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return ("Dart{" +
                "id=" +
                getId() +
                ", name='" +
                name +
                '\'' +
                ", startDate=" +
                startDate +
                ", monthlyContribution=" +
                monthlyContribution +
                ", orderMethod='" +
                orderMethod +
                ", customRules='" +
                customRules +
                '\'' +
                ", status=" +
                status +
                ", membersCount=" +
                (members != null ? members.size() : 0) +
                '}');
    }

    // Helper methods for bidirectional relationship
    public void addMember(Member member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        members.add(member);
        member.setDart(this);
    }

    public void removeMember(Member member) {
        if (members != null) {
            members.remove(member);
            member.setDart(null);
        }
    }

    public void clearMembers() {
        if (members != null) {
            members.forEach(member -> member.setDart(null));
            members.clear();
        }
    }

    // Business methods
    public boolean isActive() {
        return status == DartStatus.ACTIVE;
    }

    public boolean isPending() {
        return status == DartStatus.PENDING;
    }

    public boolean isFinished() {
        return status == DartStatus.FINISHED;
    }

    public void activate() {
        if (this.status == DartStatus.PENDING) {
            this.status = DartStatus.ACTIVE;
        } else {
            throw new IllegalStateException("Can only activate a pending dart");
        }
    }

    public void finish() {
        if (this.status == DartStatus.ACTIVE) {
            this.status = DartStatus.FINISHED;
        } else {
            throw new IllegalStateException("Can only finish an active dart");
        }
    }

    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    public List<Member> getActiveMembers() {
        if (members == null) {
            return new ArrayList<>();
        }
        return members
                .stream()
                .filter(
                        member -> member.getStatus() == com.tontin.platform.domain.enums.member.MemberStatus.ACTIVE)
                .toList();
    }

    public BigDecimal calculateTotalMonthlyContributions() {
        return monthlyContribution.multiply(
                BigDecimal.valueOf(getMemberCount()));
    }
}
