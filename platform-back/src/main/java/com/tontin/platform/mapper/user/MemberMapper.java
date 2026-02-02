package com.tontin.platform.mapper.user;

import org.mapstruct.Mapper;

import com.tontin.platform.domain.Member;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;

@Mapper(componentModel = "spring")
public interface MemberMapper {
    Member toEntity(MemberRequest request);
    MemberResponse toDto(Member member);
}
