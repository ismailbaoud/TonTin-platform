package com.tontin.platform.service;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import java.util.List;
import java.util.UUID;

public interface MemberService {
    void createMember(
        DartPermission permission,
        MemberStatus status,
        Dart dart,
        User organizer
    );

    MemberResponse addMemberToDart(MemberRequest request, UUID id, UUID dartId);
    MemberResponse updateMemberPermission(
        MemberRequest request,
        UUID id,
        UUID dartId
    );
    MemberResponse getMember(UUID id, UUID dartId);
    List<MemberResponse> getAllMembersOfDart(UUID dartId);
    String deleteMember(UUID id, UUID dartId);

    /**
     * Accept invitation to join a dart (change member status from PENDING to ACTIVE)
     *
     * @param dartId the dart ID
     * @return the updated member details
     */
    MemberResponse acceptInvitation(UUID dartId);


    /**
     * Reject invitation to join a dart (change member status from PENDING to Rejected)
     *
     * @param dartId the dart ID
     * @return the updated member details
     */
    MemberResponse rejectInvitation(UUID dartId);
}
