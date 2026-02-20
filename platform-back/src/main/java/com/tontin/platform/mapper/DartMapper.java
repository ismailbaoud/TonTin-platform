package com.tontin.platform.mapper;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.User;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DartMapper {
    Dart toEntity(DartRequest request);

    @Mapping(
        target = "totalMonthlyPool",
        expression = "java(dart.getMonthlyContribution())"
    )
    @Mapping(target = "organizerId", ignore = true)
    @Mapping(target = "organizerName", ignore = true)
    @Mapping(target = "organizerAvatar", ignore = true)
    @Mapping(target = "isOrganizer", ignore = true)
    @Mapping(target = "currentCycle", ignore = true)
    @Mapping(target = "totalCycles", ignore = true)
    @Mapping(target = "nextPayoutDate", ignore = true)
    @Mapping(target = "image", ignore = true)
    DartResponse toDto(Dart dart);

    /**
     * Maps a Dart entity to DartResponse with additional context from the current
     * user. Round statistics default to zero — use the overload that accepts
     * paidRounds/totalRounds/nextPayoutDate for a fully populated response.
     *
     * @param dart          the dart entity
     * @param currentUserId the ID of the current authenticated user
     * @return the dart response with complete information
     */
    default DartResponse toDtoWithContext(
        Dart dart,
        java.util.UUID currentUserId
    ) {
        return toDtoWithContext(
            dart,
            currentUserId,
            0L,
            dart.getMemberCount(),
            null
        );
    }

    /**
     * Maps a Dart entity to DartResponse with real round statistics.
     *
     * @param dart           the dart entity
     * @param currentUserId  the ID of the current authenticated user
     * @param paidRounds     number of rounds whose status is PAYED
     * @param totalRounds    total number of rounds created for this dart
     *                       (falls back to memberCount when no rounds exist yet)
     * @param nextPayoutDate date of the next INPAYED round, or null
     * @return the dart response with complete information
     */
    default DartResponse toDtoWithContext(
        Dart dart,
        java.util.UUID currentUserId,
        long paidRounds,
        long totalRounds,
        java.time.LocalDateTime nextPayoutDate
    ) {
        if (dart == null) {
            return null;
        }

        // Find the organizer member
        User organizer = dart
            .getMembers()
            .stream()
            .filter(
                m ->
                    m.getPermission() ==
                    com.tontin.platform.domain.enums.dart.DartPermission.ORGANIZER
            )
            .findFirst()
            .map(m -> m.getUser())
            .orElse(null);

        // Check if current user is organizer
        boolean isOrganizer = dart
            .getMembers()
            .stream()
            .anyMatch(
                m ->
                    m.getUser().getId().equals(currentUserId) &&
                    m.getPermission() ==
                    com.tontin.platform.domain.enums.dart.DartPermission.ORGANIZER
            );

        // Find current user's member record
        var currentUserMember = dart
            .getMembers()
            .stream()
            .filter(m -> m.getUser().getId().equals(currentUserId))
            .findFirst()
            .orElse(null);

        // Get current user's permission and status
        String userPermission =
            currentUserMember != null
                ? currentUserMember.getPermission().name()
                : null;
        String userMemberStatus =
            currentUserMember != null
                ? currentUserMember.getStatus().name()
                : null;

        // When no rounds have been created yet, fall back to member count
        long effectiveTotalCycles =
            totalRounds > 0 ? totalRounds : dart.getMemberCount();

        return DartResponse.builder()
            .id(dart.getId())
            .name(dart.getName())
            .monthlyContribution(dart.getMonthlyContribution())
            .startDate(dart.getStartDate())
            .orderMethod(
                dart.getOrderMethod() != null
                    ? dart.getOrderMethod().name()
                    : null
            )
            .description(dart.getDescription())
            .paymentFrequency(dart.getPaymentFrequency())
            .status(dart.getStatus())
            .memberCount(dart.getMemberCount())
            .totalMonthlyPool(dart.getMonthlyContribution())
            .organizerId(organizer != null ? organizer.getId() : null)
            .organizerName(organizer != null ? organizer.getUserName() : null)
            .organizerAvatar(
                organizer != null && organizer.getPicture() != null
                    ? java.util.Base64.getEncoder().encodeToString(
                          organizer.getPicture()
                      )
                    : null
            )
            .isOrganizer(isOrganizer)
            .userPermission(userPermission)
            .userMemberStatus(userMemberStatus)
            .currentCycle((int) paidRounds)
            .totalCycles((int) effectiveTotalCycles)
            .nextPayoutDate(nextPayoutDate)
            .image(null)
            .customRules(dart.getCustomRules())
            .createdAt(dart.getCreatedAt())
            .updatedAt(dart.getUpdatedAt())
            .build();
    }
}
