package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.domain.enums.member.MemberStatus;
import com.tontin.platform.domain.enums.notification.NotificationType;
import com.tontin.platform.domain.enums.rank.PointAction;
import com.tontin.platform.dto.member.request.MemberRequest;
import com.tontin.platform.dto.member.response.MemberResponse;
import com.tontin.platform.mapper.MemberMapper;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.MemberRepository;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.MemberService;
import com.tontin.platform.service.NotificationService;
import com.tontin.platform.service.PointsService;
import java.time.LocalDateTime;
import java.util.List;
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
public class MemberServiceImpl implements MemberService {

    private static final String MEMBER_NOT_FOUND_TEMPLATE =
        "Member not found for id %s in dart %s";
    private static final String DART_NOT_FOUND_TEMPLATE =
        "Dart not found with id: %s";
    private static final String USER_NOT_FOUND_TEMPLATE =
        "User not found with id: %s";
    private static final String LAST_ORGANIZER_CONFLICT_MESSAGE =
        "Cannot remove the last organizer from the dart";
    private static final String ORGANIZER_REQUIRED_MESSAGE =
        "Only organizers can perform this action";
    private static final String ACCESS_DENIED_MESSAGE = "Access denied";
    private static final String DART_ACTIVE_INVITE_MESSAGE =
        "Cannot invite new members to a Dâr that has already started.";
    private static final String DART_ACTIVE_REMOVE_MESSAGE =
        "Cannot remove members from a Dâr that has already started.";

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DartRepository dartRepository;
    private final MemberMapper memberMapper;
    private final SecurityUtils securityUtils;
    private final NotificationService notificationService;
    private final PointsService pointsService;

    @Override
    @Transactional
    public void createMember(
        DartPermission permission,
        MemberStatus status,
        Dart dart,
        User organizer
    ) {
        log.info(
            "Creating member with permission {} for dart {}",
            permission,
            dart != null ? dart.getId() : null
        );

        if (permission == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Permission is required"
            );
        }
        if (status == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Member status is required"
            );
        }
        if (dart == null || dart.getId() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "A persisted dart instance is required to create a member"
            );
        }
        if (organizer == null || organizer.getId() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Organizer user is required"
            );
        }

        if (
            memberRepository.existsByUserIdAndDartId(
                organizer.getId(),
                dart.getId()
            )
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "User is already a member of this dart"
            );
        }

        Member member = Member.builder()
            .permission(permission)
            .status(status)
            .joinedAt(LocalDateTime.now())
            .build();
        member.setUser(organizer);
        dart.addMember(member);

        Member savedMember = memberRepository.save(member);
        log.debug(
            "Member {} created for dart {}",
            savedMember.getId(),
            dart.getId()
        );
    }

    @Override
    @Transactional
    public MemberResponse addMemberToDart(
        MemberRequest request,
        UUID userId,
        UUID dartId
    ) {
        log.info("Adding user {} to dart {}", userId, dartId);
        validateRequest(request);
        validateUuid(userId, "userId");
        validateUuid(dartId, "dartId");
        requireOrganizer(dartId);

        User user = userRepository
            .findById(userId)
            .orElseThrow(() ->
                notFound(HttpStatus.NOT_FOUND, USER_NOT_FOUND_TEMPLATE, userId)
            );
        Dart dart = dartRepository
            .findById(dartId)
            .orElseThrow(() ->
                notFound(HttpStatus.NOT_FOUND, DART_NOT_FOUND_TEMPLATE, dartId)
            );

        if (memberRepository.existsByUserIdAndDartId(userId, dartId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "User is already a member of this dart"
            );
        }

        // Block invitations once the dart is running
        if (
            dart.getStatus() == DartStatus.ACTIVE ||
            dart.getStatus() == DartStatus.FINISHED
        ) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                DART_ACTIVE_INVITE_MESSAGE
            );
        }

        Member member = memberMapper.toEntity(request);
        member.setStatus(MemberStatus.PENDING);
        member.setJoinedAt(LocalDateTime.now());
        member.setUser(user);
        dart.addMember(member);

        Member savedMember = memberRepository.save(member);
        log.debug(
            "User {} registered as member {} for dart {}",
            userId,
            savedMember.getId(),
            dartId
        );

        // Notify the invited user (best-effort; do not fail the invite if notification fails)
        try {
            User organizer = securityUtils.requireCurrentUser();
            String organizerName = organizer.getUserName() != null ? organizer.getUserName() : "Someone";
            String dartName = dart.getName() != null ? dart.getName() : "a Dâr";
            String title = "Invitation to join a Dâr";
            String description = String.format(
                "%s invited you to join \"%s\". Open My Dârs to accept or decline.",
                organizerName,
                dartName
            );
            String actionUrl = "/dashboard/client/dar/" + dartId;
            notificationService.create(
                userId,
                NotificationType.DAR_INVITATION,
                title,
                description,
                actionUrl,
                "View invitation"
            );
        } catch (Exception e) {
            log.warn("Could not create invitation notification for user {} (invite still succeeded): {}", userId, e.getMessage());
        }

        return memberMapper.toDto(savedMember);
    }

    @Override
    @Transactional
    public MemberResponse updateMemberPermission(
        MemberRequest request,
        UUID memberId,
        UUID dartId
    ) {
        log.info(
            "Updating permission for member {} in dart {}",
            memberId,
            dartId
        );
        validateRequest(request);
        validateUuid(memberId, "memberId");
        validateUuid(dartId, "dartId");
        requireOrganizer(dartId);

        Member member = getMemberOrThrow(memberId, dartId);

        if (
            member.getPermission() == DartPermission.ORGANIZER &&
            request.permission() == DartPermission.MEMBER
        ) {
            enforceAtLeastOneOrganizer(memberId, dartId);
        }

        member.setPermission(request.permission());
        Member savedMember = memberRepository.save(member);
        log.debug(
            "Member {} permission updated to {}",
            savedMember.getId(),
            savedMember.getPermission()
        );
        return memberMapper.toDto(savedMember);
    }

    @Override
    @Transactional(readOnly = true)
    public MemberResponse getMember(UUID id, UUID dartId) {
        log.debug("Fetching member {} for dart {}", id, dartId);
        validateUuid(id, "memberId");
        validateUuid(dartId, "dartId");

        Member member = getMemberOrThrow(id, dartId);
        requireMemberAccess(member);

        return memberMapper.toDto(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MemberResponse> getAllMembersOfDart(UUID dartId) {
        log.debug("Fetching all members for dart {}", dartId);
        validateUuid(dartId, "dartId");
        ensureDartExists(dartId);
        // requireOrganizer(dartId);

        // Return ACTIVE and PENDING so organizer can manage members (remove/cancel invite) before start; exclude LEAVED
        return memberRepository
            .findAllByDartId(dartId)
            .stream()
            .filter(m -> m.getStatus() == MemberStatus.ACTIVE || m.getStatus() == MemberStatus.PENDING)
            .map(memberMapper::toDto)
            .toList();
    }

    @Override
    @Transactional
    public String deleteMember(UUID id, UUID dartId) {
        log.info("Removing member {} from dart {}", id, dartId);
        validateUuid(id, "memberId");
        validateUuid(dartId, "dartId");
        requireOrganizer(dartId);

        Member member = getMemberOrThrow(id, dartId);

        // Use the dart this member belongs to (same as dartId) to decide if removal is allowed
        Dart dart = member.getDart();
        if (dart == null) {
            dart = dartRepository
                .findById(dartId)
                .orElseThrow(() ->
                    notFound(HttpStatus.NOT_FOUND, DART_NOT_FOUND_TEMPLATE, dartId)
                );
        }
        // Block removal only once the dart has been started (ACTIVE or FINISHED).
        // Before start (dart PENDING): organizer can remove ANY member regardless of status (ACTIVE or PENDING).
        if (dart.getStatus() == DartStatus.ACTIVE || dart.getStatus() == DartStatus.FINISHED) {
            log.warn(
                "Cannot remove member {} from dart {}: dart status is {}",
                id,
                dartId,
                dart.getStatus()
            );
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                DART_ACTIVE_REMOVE_MESSAGE
            );
        }

        // Intentionally no check on member.getStatus(): organizer can remove ANY member (ACTIVE or PENDING) before dart start.
        MemberStatus memberStatus = member.getStatus();
        log.info(
            "Removing member {} (status={}) from dart {} — allowed because dart is not started",
            id,
            memberStatus,
            dartId
        );
        if (member.getPermission() == DartPermission.ORGANIZER) {
            enforceAtLeastOneOrganizer(id, dartId);
        }

        if (member.getDart() != null) {
            member.getDart().removeMember(member);
        }
        member.setUser(null);
        member.setDart(null);

        memberRepository.delete(member);
        log.debug("Member {} removed from dart {}", id, dartId);
        return "Member removed successfully";
    }

    private void validateRequest(MemberRequest request) {
        if (request == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Member request payload must not be null"
            );
        }
        if (request.permission() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Permission is required"
            );
        }
    }

    private void validateUuid(UUID id, String fieldName) {
        if (id == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                String.format("%s must not be null", fieldName)
            );
        }
    }

    private Member getMemberOrThrow(UUID memberId, UUID dartId) {
        return memberRepository
            .findByIdAndDartId(memberId, dartId)
            .orElseThrow(() ->
                notFound(
                    HttpStatus.NOT_FOUND,
                    MEMBER_NOT_FOUND_TEMPLATE,
                    memberId,
                    dartId
                )
            );
    }

    private void ensureDartExists(UUID dartId) {
        if (!dartRepository.existsById(dartId)) {
            throw notFound(
                HttpStatus.NOT_FOUND,
                DART_NOT_FOUND_TEMPLATE,
                dartId
            );
        }
    }

    private void requireOrganizer(UUID dartId) {
        UUID currentUserId = securityUtils.requireCurrentUserId();
        boolean organizer =
            memberRepository.existsByDartIdAndUserIdAndPermission(
                dartId,
                currentUserId,
                DartPermission.ORGANIZER
            );
        if (!organizer) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ORGANIZER_REQUIRED_MESSAGE
            );
        }
    }

    private void requireMemberAccess(Member member) {
        UUID currentUserId = securityUtils.requireCurrentUserId();

        UUID dartId =
            member.getDart() != null ? member.getDart().getId() : null;
        UUID memberUserId =
            member.getUser() != null ? member.getUser().getId() : null;

        boolean isOrganizer =
            dartId != null &&
            memberRepository.existsByDartIdAndUserIdAndPermission(
                dartId,
                currentUserId,
                DartPermission.ORGANIZER
            );

        if (
            !isOrganizer &&
            (memberUserId == null || !memberUserId.equals(currentUserId))
        ) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                ACCESS_DENIED_MESSAGE
            );
        }
    }

    private void enforceAtLeastOneOrganizer(UUID memberId, UUID dartId) {
        long organizerCount = memberRepository.countByDartIdAndPermission(
            dartId,
            DartPermission.ORGANIZER
        );
        if (organizerCount <= 1) {
            log.warn(
                "Attempt to remove the last organizer (member {}, dart {})",
                memberId,
                dartId
            );
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                LAST_ORGANIZER_CONFLICT_MESSAGE
            );
        }
    }

    @Override
    @Transactional
    public MemberResponse acceptInvitation(UUID dartId) {
        log.info("Accepting invitation for dart {}", dartId);
        validateUuid(dartId, "dartId");

        User currentUser = securityUtils.requireCurrentUser();

        // Find the member record for current user in this dart
        Member member = memberRepository
            .findByDartIdAndUserId(dartId, currentUser.getId())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "You are not a member of this dart"
                )
            );

        // Check if member is in PENDING status
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invitation has already been " +
                    (member.getStatus() == MemberStatus.ACTIVE
                        ? "accepted"
                        : "processed")
            );
        }

        // Update status to ACTIVE
        member.activate();
        Member savedMember = memberRepository.save(member);

        log.info(
            "User {} accepted invitation to dart {}",
            currentUser.getId(),
            dartId
        );

        // Notify organizers that the member joined
        Dart dart = member.getDart();
        String memberName = currentUser.getUserName() != null ? currentUser.getUserName() : "A member";
        String dartName = dart != null && dart.getName() != null ? dart.getName() : "the Dâr";
        String title = "Member joined your Dâr";
        String description = String.format(
            "%s accepted the invitation and joined \"%s\".",
            memberName,
            dartName
        );
        String actionUrl = "/dashboard/client/dar/" + dartId;
        for (Member m : memberRepository.findAllByDartId(dartId)) {
            if (m.getPermission() == DartPermission.ORGANIZER && m.getUser() != null) {
                UUID organizerId = m.getUser().getId();
                if (!organizerId.equals(currentUser.getId())) {
                    notificationService.create(
                        organizerId,
                        NotificationType.MEMBER_JOINED,
                        title,
                        description,
                        actionUrl,
                        "View Dâr"
                    );
                    pointsService.addPoints(organizerId, PointAction.MEMBER_JOINED_YOUR_DAR, dartId);
                }
            }
        }
        pointsService.addPoints(currentUser.getId(), PointAction.INVITATION_ACCEPTED, dartId);

        return memberMapper.toDto(savedMember);
    }

    private ResponseStatusException notFound(
        HttpStatus status,
        String template,
        Object... args
    ) {
        String message = String.format(template, args);
        return new ResponseStatusException(status, message);
    }

    @Override
    public MemberResponse rejectInvitation(UUID dartId) {
        log.info("Rejecting invitation for dart {}", dartId);
        validateUuid(dartId, "dartId");

        User currentUser = securityUtils.requireCurrentUser();

        // Find the member record for current user in this dart
        Member member = memberRepository
            .findByDartIdAndUserId(dartId, currentUser.getId())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "You are not a member of this dart"
                )
            );

        // Check if member is in PENDING status
        if (member.getStatus() != MemberStatus.PENDING) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invitation has already been " +
                    (member.getStatus() == MemberStatus.ACTIVE
                        ? "accepted"
                        : "processed")
            );
        }

        // update status to LEAVED
        member.reject();
        Member savedMember = memberRepository.save(member);

        log.info(
            "User {} rejected invitation to dart {}",
            currentUser.getId(),
            dartId
        );
        pointsService.addPoints(currentUser.getId(), PointAction.INVITATION_REJECTED, dartId);

        // Notify organizers that the member declined
        Dart dart = member.getDart();
        String memberName = currentUser.getUserName() != null ? currentUser.getUserName() : "A user";
        String dartName = dart != null && dart.getName() != null ? dart.getName() : "the Dâr";
        String title = "Invitation declined";
        String description = String.format(
            "%s declined the invitation to join \"%s\".",
            memberName,
            dartName
        );
        String actionUrl = "/dashboard/client/dar/" + dartId;
        for (Member m : memberRepository.findAllByDartId(dartId)) {
            if (m.getPermission() == DartPermission.ORGANIZER && m.getUser() != null) {
                UUID organizerId = m.getUser().getId();
                if (!organizerId.equals(currentUser.getId())) {
                    notificationService.create(
                        organizerId,
                        NotificationType.MEMBER_LEFT,
                        title,
                        description,
                        actionUrl,
                        "View Dâr"
                    );
                }
            }
        }

        return memberMapper.toDto(savedMember);
    }
}
