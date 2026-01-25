package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import com.tontin.platform.mapper.DartMapper;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.service.DartService;
import com.tontin.platform.service.MemberService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DartServiceImpl implements DartService {

    private final DartRepository dartRepository;
    private final MemberService memberService;
    private final DartMapper dartMapper;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public DartResponse createDart(DartRequest request) {
        log.info(
            "Creating dart with name: {}",
            request != null ? request.name() : null
        );
        validateRequest(request);
        Dart dart = dartMapper.toEntity(request);
        dart.setStatus(DartStatus.PENDING);
        dart.setStartDate(LocalDateTime.now());
        Dart savedDart = dartRepository.save(dart);
        log.debug("Dart persisted with id: {}", savedDart.getId());

        User organizer = securityUtils.requireCurrentUser();

        memberService.createMember(
            DartPermission.ORGANIZER,
            MemberStatus.ACTIVE,
            savedDart,
            organizer
        );
        log.debug(
            "Organizer member created for dart id: {}",
            savedDart.getId()
        );

        return dartMapper.toDto(savedDart);
    }

    @Override
    @Transactional
    public DartResponse updateDart(DartRequest request, UUID id) {
        log.info("Updating dart with id: {}", id);
        validateRequest(request);
        validateId(id);
        Dart dart = findDartById(id);
        applyRequest(dart, request);
        Dart updatedDart = dartRepository.save(dart);
        log.debug("Dart updated with id: {}", updatedDart.getId());
        return dartMapper.toDto(updatedDart);
    }

    @Override
    @Transactional(readOnly = true)
    public DartResponse getDartDetails(UUID id) {
        log.debug("Fetching dart details for id: {}", id);
        validateId(id);
        Dart dart = findDartById(id);
        return dartMapper.toDto(dart);
    }

    @Override
    @Transactional
    public DartResponse deleteDart(UUID id) {
        log.info("Deleting dart with id: {}", id);
        validateId(id);
        Dart dart = findDartById(id);
        if (dart.isActive() && !dart.getActiveMembers().isEmpty()) {
            log.warn(
                "Attempt to delete active dart {} with active members",
                id
            );
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Cannot delete an active dart with active members"
            );
        }
        DartResponse response = dartMapper.toDto(dart);
        dartRepository.delete(dart);
        log.debug("Dart deleted with id: {}", id);
        return response;
    }

    private void validateId(UUID id) {
        if (id == null) {
            log.error("Dart identifier is null");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dart identifier must not be null"
            );
        }
    }

    private Dart findDartById(UUID id) {
        return dartRepository
            .findById(id)
            .orElseThrow(() -> {
                log.warn("Dart not found with id: {}", id);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Dart not found with id: " + id
                );
            });
    }

    private void applyRequest(Dart dart, DartRequest request) {
        dart.setName(request.name());
        dart.setMonthlyContribution(request.monthlyContribution());
        dart.setAllocationMethod(request.allocationMethod());
    }

    private void validateRequest(DartRequest request) {
        if (request == null) {
            log.error("Dart request payload is null");
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Dart request must not be null"
            );
        }
        if (!request.isValidAllocationMethod()) {
            log.error(
                "Unsupported allocation method received: {}",
                request.allocationMethod()
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported allocation method: " + request.allocationMethod()
            );
        }
    }
}
