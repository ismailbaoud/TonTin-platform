package com.tontin.platform.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tontin.platform.domain.enums.user.UserRole;
import com.tontin.platform.domain.enums.user.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "users")
@Getter
@Setter
public class User  {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_name")
    private String userName;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    private UserRole role;
    private UserStatus status;

    @OneToOne(mappedBy = "user")
    private Member member;

}
