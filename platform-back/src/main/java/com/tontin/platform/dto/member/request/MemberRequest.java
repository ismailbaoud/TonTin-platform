package com.tontin.platform.dto.member.request;

import com.tontin.platform.domain.enums.dart.DartPermession;

import lombok.Data;

@Data
public class MemberRequest {
    private DartPermession permession;
}
