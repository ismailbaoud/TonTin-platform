package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.member.MemberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member extends BaseEntity {

    @NotNull(message = "Permission is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 20)
    private DartPermission permission;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private MemberStatus status = MemberStatus.PENDING;

    @NotNull(message = "Joined date is required")
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dart_id", nullable = false)
    private Dart dart;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Business key equals/hashCode based on user and dart
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return (
            user != null &&
            user.equals(member.user) &&
            dart != null &&
            dart.equals(member.dart)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, dart);
    }

    @Override
    public String toString() {
        return (
            "Member{" +
            "id=" +
            getId() +
            ", permission=" +
            permission +
            ", status=" +
            status +
            ", joinedAt=" +
            joinedAt +
            ", userId=" +
            (user != null ? user.getId() : null) +
            ", dartId=" +
            (dart != null ? dart.getId() : null) +
            '}'
        );
    }

    // Helper methods for bidirectional relationships
    public void setDart(Dart dart) {
        this.dart = dart;
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getMember() != this) {
            user.setMember(this);
        }
    }

    // Business methods
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isPending() {
        return status == MemberStatus.PENDING;
    }

    public boolean hasLeft() {
        return status == MemberStatus.LEAVED;
    }

    public boolean isOrganizer() {
        return permission == DartPermission.ORGANIZER;
    }

    public boolean isMember() {
        return permission == DartPermission.MEMBER;
    }

    public void activate() {
        if (this.status == MemberStatus.PENDING) {
            this.status = MemberStatus.ACTIVE;
        } else {
            throw new IllegalStateException(
                "Can only activate a pending member"
            );
        }
    }

    public void leave() {
        if (this.status == MemberStatus.ACTIVE) {
            this.status = MemberStatus.LEAVED;
        } else {
            throw new IllegalStateException(
                "Can only leave if member is active"
            );
        }
    }

    public void promoteToOrganizer() {
        if (this.permission == DartPermission.MEMBER) {
            this.permission = DartPermission.ORGANIZER;
        }
    }

    public void demoteToMember() {
        if (this.permission == DartPermission.ORGANIZER) {
            this.permission = DartPermission.MEMBER;
        }
    }

    public long getDaysSinceJoining() {
        if (joinedAt == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(
            joinedAt,
            LocalDateTime.now()
        );
    }
}
