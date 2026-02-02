package com.tontin.platform.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import com.tontin.platform.mapper.user.DartMapper;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.service.MemberService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService{

    private final MemberRepository memberRepository;
    private final DartMapper dartMapper;
    
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
