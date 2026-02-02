package com.tontin.platform.service;

import java.util.List;
import java.util.UUID;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.enums.dart.DartPermession;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;

public interface MemberService {
    void createMember(DartPermession permession , MemberStatus status, Dart dart);
    MemberResponse addMemberToDart(MemberRequest request, UUID id, UUID dartId);
    MemberResponse updateMemberPermession(MemberRequest request, UUID id , UUID dartId);
    MemberResponse getMember(UUID id, UUID dartId);
    List<MemberResponse> getAllMembersOfDart(UUID dartId);
    String deleteMember(UUID id, UUID dartId);

}
