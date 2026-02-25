package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Round;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.domain.enums.rank.PointAction;
import com.tontin.platform.domain.enums.round.RoundStatus;
import com.tontin.platform.domain.Member;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.request.StartDartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.mapper.DartMapper;
import com.tontin.platform.repository.DartMessageRepository;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.repository.RoundRepository;
import com.tontin.platform.service.DartService;
import com.tontin.platform.service.MemberService;
import com.tontin.platform.service.NotificationService;
import com.tontin.platform.service.PointsService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
@RequiredArgsConstructor
public class DartServiceImpl implements DartService {

    private final DartRepository dartRepository;
    private final DartMessageRepository dartMessageRepository;
    private final MemberRepository memberRepository;
    private final RoundRepository roundRepository;
    private final MemberService memberService;
    private final DartMapper dartMapper;
    private final SecurityUtils securityUtils;
    private final com.tontin.platform.service.RoundService roundService;
    private final NotificationService notificationService;
    private final PointsService pointsService;

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Builds a fully-populated DartResponse with real round statistics.
     * Uses 3 lightweight queries: paid count, total count, and next payout date.
     */
    private DartResponse buildResponse(Dart dart, UUID currentUserId) {
        UUID dartId = dart.getId();
        long paidRounds = roundRepository.countPaidRoundsByDartId(dartId);
        long totalRounds = roundRepository.countByDartId(dartId);
        LocalDateTime nextPayoutDate = roundRepository
            .findAllByDartIdAndStatus(dartId, RoundStatus.INPAYED)
            .stream()
            .findFirst()
            .map(Round::getDate)
            .orElse(null);
        return dartMapper.toDtoWithContext(
            dart,
            currentUserId,
            paidRounds,
            totalRounds,
            nextPayoutDate
        );
    }

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
        dart.setStartDate(null); // Will be set when organizer starts the dart
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
        pointsService.addPoints(organizer.getId(), PointAction.DAR_CREATED, savedDart.getId());

        // Newly created dart has no rounds yet
        return dartMapper.toDtoWithContext(
            savedDart,
            organizer.getId(),
            0L,
            0L,
            null
        );
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
        User currentUser = securityUtils.requireCurrentUser();
        return buildResponse(updatedDart, currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public DartResponse getDartDetails(UUID id) {
        log.debug("Fetching dart details for id: {}", id);
        validateId(id);
        Dart dart = findDartById(id);
        User currentUser = securityUtils.requireCurrentUser();
        var member = memberRepository.findByDartIdAndUserId(id, currentUser.getId());
        if (member.isEmpty() || member.get().getStatus() == MemberStatus.LEAVED) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You no longer have access to this Dâr."
            );
        }
        return buildResponse(dart, currentUser.getId());
    }

    @Override
    @Transactional
    public DartResponse deleteDart(UUID id) {
        log.info("Deleting dart with id: {}", id);
        validateId(id);
        Dart dart = findDartById(id);
        if (dart.getStatus() != DartStatus.PENDING) {
            log.warn("Attempt to delete dart {} which is not PENDING (status: {})", id, dart.getStatus());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You can only delete a Dâr before it has started."
            );
        }
        User currentUser = securityUtils.requireCurrentUser();
        DartResponse response = buildResponse(dart, currentUser.getId());
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
        dart.setOrderMethod(request.orderMethod());
        dart.setDescription(request.description());
        dart.setPaymentFrequency(request.paymentFrequency());
        if (request.customRules() != null) {
            dart.setCustomRules(request.customRules());
        }
        if (request.picture() != null) {
            dart.setPicture(
                request.picture().length == 0 ? null : request.picture()
            );
        }
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
                "Unsupported order method received: {}",
                request.orderMethod()
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported order method: " + request.orderMethod()
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DartResponse> getMyDarts(
        DartStatus status,
        int page,
        int pageSize
    ) {
        log.info(
            "Fetching darts for current user with status: {}, page: {}, size: {}",
            status,
            page,
            pageSize
        );

        User currentUser = securityUtils.requireCurrentUser();
        Pageable pageable = PageRequest.of(page, pageSize);

        Page<Dart> dartPage;
        if (status != null) {
            dartPage = dartRepository.findAllByUserIdAndStatus(
                currentUser.getId(),
                status,
                pageable
            );
        } else {
            dartPage = dartRepository.findAllByUserId(
                currentUser.getId(),
                pageable
            );
        }
        log.info("Total elements from DB: {}", dartPage.getTotalElements());
        log.info(
            "Content size before mapping: {}",
            dartPage.getContent().size()
        );
        dartPage
            .getContent()
            .forEach(d -> log.info("Dart ID from DB: {}", d.getId()));
        log.info("Current user ID: {}", currentUser.getId());

        Page<DartResponse> responsePage = dartPage.map(dart ->
            buildResponse(dart, currentUser.getId())
        );

        log.debug(
            "Retrieved {} darts for user {}",
            responsePage.getNumberOfElements(),
            currentUser.getId()
        );
        System.out.println(responsePage);

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional
    public DartResponse startDart(UUID id, StartDartRequest request) {
        log.info("Starting dart with id: {}", id);
        validateId(id);
        boolean startAnyway = request != null && request.startAnyway();

        Dart dart = findDartById(id);
        User currentUser = securityUtils.requireCurrentUser();

        // Verify user is organizer
        boolean isOrganizer = dart
            .getMembers()
            .stream()
            .anyMatch(
                m ->
                    m.getUser().getId().equals(currentUser.getId()) &&
                    m.getPermission() == DartPermission.ORGANIZER
            );

        if (!isOrganizer) {
            log.warn(
                "User {} is not organizer of dart {}",
                currentUser.getId(),
                id
            );
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only organizers can start a dart"
            );
        }

        // Check if dart is already started
        if (dart.getStartDate() != null) {
            log.warn("Dart {} is already started", id);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Dart has already been started"
            );
        }

        // Check for pending members: block start unless startAnyway
        List<Member> pendingMembers = dart
            .getMembers()
            .stream()
            .filter(m -> m.getStatus() == MemberStatus.PENDING)
            .toList();
        if (!pendingMembers.isEmpty() && !startAnyway) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You can't start the dart while there are pending users. Start anyway to remove them from the dart."
            );
        }
        if (!pendingMembers.isEmpty() && startAnyway) {
            for (Member m : pendingMembers) {
                m.setStatus(MemberStatus.LEAVED);
                memberRepository.save(m);
            }
            log.info("Set {} pending member(s) to LEAVED for dart {}", pendingMembers.size(), id);
        }

        // Check minimum active members (at least 2 including organizer)
        if (dart.getActiveMembers().size() < 2) {
            log.warn("Dart {} does not have minimum members", id);
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot start dart: minimum 2 active members required"
            );
        }

        // Set start date and change status to ACTIVE
        dart.setStartDate(LocalDateTime.now());
        dart.setStatus(DartStatus.ACTIVE);

        Dart updatedDart = dartRepository.save(dart);
        log.info("Dart {} started successfully", id);

        // Notify all active members that the Dâr has started
        String dartName = updatedDart.getName() != null ? updatedDart.getName() : "Your Dâr";
        String title = "Dâr started";
        String description = String.format("\"%s\" has started. You can now view rounds and make contributions.", dartName);
        String actionUrl = "/dashboard/client/dar/" + id;
        for (var m : updatedDart.getActiveMembers()) {
            if (m.getUser() != null) {
                notificationService.create(
                    m.getUser().getId(),
                    NotificationType.SYSTEM,
                    title,
                    description,
                    actionUrl,
                    "View Dâr"
                );
            }
        }
        pointsService.addPoints(currentUser.getId(), PointAction.DAR_STARTED, id);

        // Automatically create rounds for the dart based on order method and payment frequency
        try {
            com.tontin.platform.dto.round.request.CreateRoundsRequest createRoundsRequest =
                new com.tontin.platform.dto.round.request.CreateRoundsRequest(
                    id
                );
            roundService.createRoundsForDart(createRoundsRequest);
            log.info("Rounds created successfully for dart {}", id);
        } catch (Exception e) {
            log.error(
                "Failed to create rounds for dart {}: {}",
                id,
                e.getMessage(),
                e
            );
            // Don't fail the dart start if round creation fails - rounds can be created manually later
            // But log the error for investigation
        }

        return buildResponse(updatedDart, currentUser.getId());
    }

    @Override
    @Transactional
    public DartResponse finishDart(UUID id) {
        log.info("Finishing dart with id: {}", id);
        validateId(id);
        Dart dart = findDartById(id);
        User currentUser = securityUtils.requireCurrentUser();

        boolean isOrganizer = dart
            .getMembers()
            .stream()
            .anyMatch(
                m ->
                    m.getUser().getId().equals(currentUser.getId()) &&
                    m.getPermission() == DartPermission.ORGANIZER
            );

        if (!isOrganizer) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Only organizers can finish a dart"
            );
        }

        dartMessageRepository.deleteAllByDartId(id);
        dart.finish();
        Dart updatedDart = dartRepository.save(dart);
        log.info("Dart {} finished; messages deleted", id);
        return buildResponse(updatedDart, currentUser.getId());
    }
}
