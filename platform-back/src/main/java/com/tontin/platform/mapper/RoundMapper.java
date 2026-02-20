package com.tontin.platform.mapper;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Round;
import com.tontin.platform.dto.round.request.RoundRequest;
import com.tontin.platform.dto.round.response.RoundResponse;
import java.util.List;
import java.util.UUID;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Round entities and DTOs.
 */
@Mapper(componentModel = "spring")
public interface RoundMapper {
    /**
     * Maps a RoundRequest to a Round entity.
     * Note: dart must be set separately as it's not part of the request.
     *
     * @param request the round request DTO
     * @return the round entity
     */
    @Mapping(target = "dart", ignore = true)
    @Mapping(
        target = "amount",
        expression = "java(request.amount() != null ? request.amount().doubleValue() : null)"
    )
    Round toEntity(RoundRequest request);

    /**
     * Maps a Round entity to a RoundResponse DTO.
     * paidMemberIds will be null — use toDtoWithDart for a fully populated response.
     *
     * @param round the round entity
     * @return the round response DTO
     */
    @Mapping(
        target = "dartId",
        expression = "java(round.getDart() != null ? round.getDart().getId() : null)"
    )
    @Mapping(
        target = "dartName",
        expression = "java(round.getDart() != null ? round.getDart().getName() : null)"
    )
    @Mapping(
        target = "amount",
        expression = "java(round.getAmount() != null ? java.math.BigDecimal.valueOf(round.getAmount()) : null)"
    )
    @Mapping(
        target = "recipientMemberId",
        expression = "java(round.getRecipient() != null ? round.getRecipient().getId() : null)"
    )
    @Mapping(
        target = "recipientMemberName",
        expression = "java(round.getRecipient() != null && round.getRecipient().getUser() != null ? round.getRecipient().getUser().getUserName() : null)"
    )
    @Mapping(
        target = "recipientMemberEmail",
        expression = "java(round.getRecipient() != null && round.getRecipient().getUser() != null ? round.getRecipient().getUser().getEmail() : null)"
    )
    @Mapping(target = "paidMemberIds", ignore = true)
    RoundResponse toDto(Round round);

    /**
     * Maps a Round entity to RoundResponse with explicit dart information.
     * paidMemberIds will be an empty list.
     *
     * @param round the round entity
     * @param dart  the associated dart entity
     * @return the round response DTO
     */
    default RoundResponse toDtoWithDart(Round round, Dart dart) {
        return toDtoWithDart(round, dart, List.of());
    }

    /**
     * Maps a Round entity to RoundResponse with explicit dart information and
     * the list of member IDs that have already paid their contribution for this round.
     *
     * @param round          the round entity
     * @param dart           the associated dart entity
     * @param paidMemberIds  list of payer Member UUIDs whose payment is PAYED
     * @return the round response DTO
     */
    default RoundResponse toDtoWithDart(
        Round round,
        Dart dart,
        List<UUID> paidMemberIds
    ) {
        if (round == null) {
            return null;
        }

        // Extract recipient information
        UUID recipientMemberId = null;
        String recipientMemberName = null;
        String recipientMemberEmail = null;

        if (round.getRecipient() != null) {
            recipientMemberId = round.getRecipient().getId();
            if (round.getRecipient().getUser() != null) {
                recipientMemberName = round
                    .getRecipient()
                    .getUser()
                    .getUserName();
                recipientMemberEmail = round
                    .getRecipient()
                    .getUser()
                    .getEmail();
            }
        }

        return RoundResponse.builder()
            .id(round.getId())
            .number(round.getNumber())
            .status(round.getStatus())
            .date(round.getDate())
            .amount(
                round.getAmount() != null
                    ? java.math.BigDecimal.valueOf(round.getAmount())
                    : null
            )
            .dartId(dart != null ? dart.getId() : null)
            .dartName(dart != null ? dart.getName() : null)
            .recipientMemberId(recipientMemberId)
            .recipientMemberName(recipientMemberName)
            .recipientMemberEmail(recipientMemberEmail)
            .paidMemberIds(paidMemberIds != null ? paidMemberIds : List.of())
            .createdAt(round.getCreatedAt())
            .updatedAt(round.getUpdatedAt())
            .build();
    }
}
