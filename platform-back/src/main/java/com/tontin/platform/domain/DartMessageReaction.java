package com.tontin.platform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A reaction (emoji) on a dart message by a member.
 */
@Entity
@Table(
    name = "dart_message_reactions",
    uniqueConstraints = @jakarta.persistence.UniqueConstraint(columnNames = { "message_id", "member_id", "emoji" })
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DartMessageReaction extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id", nullable = false)
    private DartMessage message;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotBlank
    @Size(max = 20)
    @Column(name = "emoji", nullable = false, length = 20)
    private String emoji;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DartMessageReaction that = (DartMessageReaction) o;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
