package com.tontin.platform.mapper;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.User;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

    public Member toEntity(MemberRequest request) {
        if (request == null) {
            return null;
        }

        return Member.builder().permission(request.permission()).build();
    }

    public MemberResponse toDto(Member member) {
        if (member == null) {
            return null;
        }

        return MemberResponse.builder()
            .id(member.getId())
            .permission(member.getPermission())
            .status(member.getStatus())
            .joinedAt(member.getJoinedAt())
            .user(mapUser(member.getUser()))
            .dart(mapDart(member.getDart()))
            .createdAt(member.getCreatedAt())
            .updatedAt(member.getUpdatedAt())
            .build();
    }

    private MemberResponse.UserInfo mapUser(User user) {
        if (user == null) {
            return null;
        }

        return MemberResponse.UserInfo.builder()
            .id(user.getId())
            .userName(user.getUserName())
            .email(user.getEmail())
            .build();
    }

    private MemberResponse.DartInfo mapDart(Dart dart) {
        if (dart == null) {
            return null;
        }

        return MemberResponse.DartInfo.builder()
            .id(dart.getId())
            .name(dart.getName())
            .monthlyContribution(dart.getMonthlyContribution())
            .build();
    }
}
