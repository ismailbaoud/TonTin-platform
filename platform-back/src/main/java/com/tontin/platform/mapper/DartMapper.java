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
     * user.
     *
     * @param dart          the dart entity
     * @param currentUserId the ID of the current authenticated user
     * @return the dart response with complete information
     */
    default DartResponse toDtoWithContext(
        Dart dart,
        java.util.UUID currentUserId
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
            .currentCycle(0) // TODO: Calculate from rounds/payments
            .totalCycles(dart.getMemberCount()) // Total cycles = member count
            .nextPayoutDate(null) // TODO: Calculate from payment schedule
            .image(null) // TODO: Add image field to Dart entity if needed
            .customRules(dart.getCustomRules())
            .createdAt(dart.getCreatedAt())
            .updatedAt(dart.getUpdatedAt())
            .build();
    }
}
