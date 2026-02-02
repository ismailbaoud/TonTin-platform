package com.tontin.platform.dto.member.response;

import java.time.LocalDateTime;

import com.tontin.platform.domain.enums.dart.DartPermession;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.Dart.response.DartResponse;

public record MemberResponse (
    String userName,
    String email,
    DartPermession permession,
    MemberStatus status,
    LocalDateTime joinedAt,
    DartResponse dart
){}
