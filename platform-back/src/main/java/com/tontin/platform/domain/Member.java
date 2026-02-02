package com.tontin.platform.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tontin.platform.domain.enums.dart.DartPermession;
import com.tontin.platform.domain.enums.member.MemberStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity(name = "members")
@Getter
@Setter
@Builder
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private DartPermession permession;

    private MemberStatus status;

    private LocalDateTime joinedAt;


}
