package com.tontin.platform.domain;

import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @NotBlank(message = "Username is required")
    @Size(
        min = 3,
        max = 50,
        message = "Username must be between 3 and 50 characters"
    )
    @Column(name = "user_name", nullable = false, unique = true, length = 50)
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @NotBlank(message = "Creation date is required")
    @Column(name = "creation_date", nullable = false, length = 30)
    private String creationDate;

    @NotNull(message = "Email confirmation flag is required")
    @Column(name = "email_confirmed", nullable = false)
    @Builder.Default
    private Boolean emailConfirmed = Boolean.FALSE;

    @NotNull(message = "Access file count is required")
    @Column(name = "account_access_file_count", nullable = false)
    @Builder.Default
    private Integer accountAccessFileCount = 0;

    @Column(name = "reset_password_date")
    private LocalDateTime resetPasswordDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @Builder.Default
    private UserRole role = UserRole.ROLE_CLIENT;

    @Column(name = "picture", columnDefinition = "bytea")
    private byte[] picture;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.PENDING;

    @Column(name = "points", nullable = false)
    @Builder.Default
    private Integer points = 0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
private List<Member> members = new ArrayList<>();
    // Business key equals/hashCode based on email (natural key)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return email != null && email.equals(user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return (
            "User{" +
            "id=" +
            getId() +
            ", userName='" +
            userName +
            '\'' +
            ", email='" +
            email +
            '\'' +
            ", creationDate='" +
            creationDate +
            '\'' +
            ", emailConfirmed=" +
            emailConfirmed +
            ", accountAccessFileCount=" +
            accountAccessFileCount +
            ", resetPasswordDate=" +
            resetPasswordDate +
            ", role=" +
            role +
            ", status=" +
            status +
            '}'
        );
    }

public void addMember(Member member) {
    if (members == null) {
        members = new ArrayList<>();
    }
    members.add(member);
    member.setUser(this);
}



    // Business methods
    public boolean isVerified() {
        return (
            Boolean.TRUE.equals(emailConfirmed) && status == UserStatus.ACTIVE
        );
    }

    public boolean isPending() {
        return status == UserStatus.PENDING;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
        this.emailConfirmed = Boolean.TRUE;
        this.verificationCode = null;
    }

    public void suspend() {
        this.status = UserStatus.SUSPENDED;
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
    }
}
