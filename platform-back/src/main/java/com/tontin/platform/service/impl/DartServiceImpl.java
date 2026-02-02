package com.tontin.platform.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.dart.DartPermession;
import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.Dart.request.DartRequest;
import com.tontin.platform.dto.Dart.response.DartResponse;
import com.tontin.platform.mapper.user.DartMapper;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.service.DartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DartServiceImpl implements DartService {

    private final DartRepository dartRepository;
    private final DartMapper dartMapper;

    @Override
    public DartResponse createDart(DartRequest request) {

        Dart dart = dartMapper.toEntity(request);
        dart.setStatus(DartStatus.PENDING);
        dart.setStartDate(LocalDateTime.now());
        DartResponse dartRes = dartMapper.toDto(dartRepository.save(dart));
        Member member = Member.builder()
        .joinedAt(LocalDateTime.now())
        .permession(DartPermession.ORGANIZER)
        .status(MemberStatus.PENDING)
        .build();
        return dartRes;

    }

    @Override
    public DartResponse updateDart(DartRequest request, UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateDart'");
    }

    @Override
    public DartResponse getDartDetails(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getDartDetails'");
    }

    @Override
    public DartResponse deleteDart(UUID id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteDart'");
    }
    
}
