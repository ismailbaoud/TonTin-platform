package com.tontin.platform.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.dart.DartPermession;
import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import com.tontin.platform.mapper.user.DartMapper;
import com.tontin.platform.mapper.user.MemberMapper;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final UserRepository userRepository;

    @Override
    public void createMember(DartPermession permession , MemberStatus status, Dart dart) {
        User user = userRepository.findById(UUID.fromString("2995b69a-f056-4b56-8884-2647a4eadeb9")).orElseThrow();
        Member member = Member.builder()
        .joinedAt(LocalDateTime.now())
        .permession(permession)
        .status(status)
        .dart(dart)
        .user(user)
        .build();
        memberRepository.save(member);
    }

    @Override
    public MemberResponse addMemberToDart(MemberRequest request, UUID id, UUID dartId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createMember'");
    }

    @Override
    public MemberResponse updateMemberPermession(MemberRequest request, UUID id, UUID dartId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateMemberPermession'");
    }

    @Override
    public MemberResponse getMember(UUID id, UUID dartId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getMember'");
    }

    @Override
    public String deleteMember(UUID id, UUID dartId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteMember'");
    }

    @Override
    public List<MemberResponse> getAllMembersOfDart(UUID dartId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllMembersOfDart'");
    }
    
}
