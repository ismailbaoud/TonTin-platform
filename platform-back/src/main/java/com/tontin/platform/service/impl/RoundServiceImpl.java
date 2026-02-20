package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.Payment;
import com.tontin.platform.domain.Round;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.payment.PaymentStatus;
import com.tontin.platform.domain.enums.round.RoundStatus;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.round.request.CreateRoundsRequest;
import com.tontin.platform.dto.round.request.RoundRequest;
import com.tontin.platform.dto.round.response.RoundResponse;
import com.tontin.platform.mapper.RoundMapper;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.PaymentRepository;
import com.tontin.platform.repository.RoundRepository;
import com.tontin.platform.service.RoundOrderService;
import com.tontin.platform.service.RoundService;
import com.tontin.platform.util.PaymentFrequencyUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service implementation for Round management operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final DartRepository dartRepository;
    private final PaymentRepository paymentRepository;
    private final RoundMapper roundMapper;
    private final SecurityUtils securityUtils;
    private final RoundOrderService roundOrderService;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Build a map of roundId → list of payer Member UUIDs that have a PAYED
     * payment, using a single batch query for the entire dart.
     */
    private Map<UUID, List<UUID>> buildPaidMemberIdsByRound(UUID dartId) {
        List<Payment> paidPayments =
            paymentRepository.findAllByDartIdAndPaymentStatus(
                dartId,
                PaymentStatus.PAYED
            );

        return paidPayments
            .stream()
            .filter(p -> p.getRound() != null && p.getPayer() != null)
            .collect(
                Collectors.groupingBy(
                    p -> p.getRound().getId(),
                    Collectors.mapping(
                        p -> p.getPayer().getId(),
                        Collectors.toList()
                    )
                )
            );
    }

    /**
     * Return the list of payer Member UUIDs that have a PAYED payment for a
     * single round (used when only one round is needed).
     */
    private List<UUID> buildPaidMemberIdsForRound(UUID roundId) {
        return paymentRepository
            .findAllByRoundIdAndStatus(roundId, PaymentStatus.PAYED)
            .stream()
            .filter(p -> p.getPayer() != null)
            .map(p -> p.getPayer().getId())
            .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Service methods
    // -------------------------------------------------------------------------

    @Override
    @Transactional
    public List<RoundResponse> createRoundsForDart(
        CreateRoundsRequest request
    ) {
        log.info("Creating rounds for dart with ID: {}", request.dartId());

        Dart dart = findDartById(request.dartId());

        if (dart.getStartDate() == null) {
            log.warn(
                "Cannot create rounds for dart {} - dart not started yet",
                request.dartId()
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot create rounds: dart must be started first"
            );
        }

        long existingRounds = roundRepository.countByDartId(request.dartId());
        if (existingRounds > 0) {
            log.warn(
                "Rounds already exist for dart {} - found {} rounds",
                request.dartId(),
                existingRounds
            );
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Rounds already exist for this dart. Cannot create duplicates."
            );
        }

        List<Member> activeMembers = dart.getActiveMembers();
        if (activeMembers.size() < 2) {
            log.warn(
                "Cannot create rounds for dart {} - insufficient members: {}",
                request.dartId(),
                activeMembers.size()
            );
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot create rounds: minimum 2 active members required"
            );
        }

        List<Member> orderedMembers = roundOrderService.determineMemberOrder(
            activeMembers,
            dart.getOrderMethod()
        );

        log.info(
            "Determined member order for dart {}: {} members",
            request.dartId(),
            orderedMembers.size()
        );
        orderedMembers.forEach(m ->
            log.debug("  - Member {}: {}", m.getId(), m.getUser().getUserName())
        );

        double roundAmount = dart
            .getMonthlyContribution()
            .multiply(BigDecimal.valueOf(activeMembers.size()))
            .doubleValue();

        List<Round> rounds = new ArrayList<>();
        LocalDateTime startDate = dart.getStartDate();

        for (int i = 0; i < orderedMembers.size(); i++) {
            Member recipient = orderedMembers.get(i);
            int roundNumber = i + 1;

            LocalDateTime roundDate = PaymentFrequencyUtil.calculateRoundDate(
                startDate,
                dart.getPaymentFrequency(),
                roundNumber
            );

            Round round = Round.builder()
                .number(roundNumber)
                .status(RoundStatus.INPAYED)
                .date(roundDate)
                .amount(roundAmount)
                .dart(dart)
                .recipient(recipient)
                .build();

            rounds.add(round);
            log.debug(
                "Created round {} for dart {} - recipient: {}, date: {}",
                roundNumber,
                request.dartId(),
                recipient.getUser().getUserName(),
                roundDate
            );
        }

        List<Round> savedRounds = roundRepository.saveAll(rounds);
        log.info(
            "Successfully created {} rounds for dart {}",
            savedRounds.size(),
            request.dartId()
        );

        // Newly created rounds have no payments yet; pass empty lists.
        return savedRounds
            .stream()
            .map(round -> roundMapper.toDtoWithDart(round, dart, List.of()))
            .toList();
    }

    @Override
    @Transactional
    public RoundResponse createRound(UUID dartId, RoundRequest request) {
        log.info("Creating round {} for dart {}", request.number(), dartId);
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round creation not yet implemented"
        );
    }

    @Override
    @Transactional
    public RoundResponse updateRound(
        UUID dartId,
        UUID roundId,
        RoundRequest request
    ) {
        log.info("Updating round {} for dart {}", roundId, dartId);
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round update not yet implemented"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getRoundById(UUID dartId, UUID roundId) {
        log.debug("Fetching round {} for dart {}", roundId, dartId);
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round retrieval not yet implemented"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> getAllRoundsByDartId(UUID dartId) {
        log.debug("Fetching all rounds for dart {}", dartId);

        Dart dart = findDartById(dartId);
        List<Round> rounds = roundRepository.findAllByDartId(dartId);
        log.info("Found {} rounds for dart {}", rounds.size(), dartId);

        // Single batch query — no N+1
        Map<UUID, List<UUID>> paidMap = buildPaidMemberIdsByRound(dartId);

        return rounds
            .stream()
            .map(round ->
                roundMapper.toDtoWithDart(
                    round,
                    dart,
                    paidMap.getOrDefault(round.getId(), List.of())
                )
            )
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoundResponse> getAllRoundsByDartId(
        UUID dartId,
        int page,
        int size
    ) {
        log.info(
            "Fetching rounds for dart {} with pagination - page: {}, size: {}",
            dartId,
            page,
            size
        );

        Dart dart = findDartById(dartId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Round> roundPage = roundRepository.findAllByDartId(
            dartId,
            pageable
        );

        // Batch-load paid member IDs for the full dart (simpler than fetching per page)
        Map<UUID, List<UUID>> paidMap = buildPaidMemberIdsByRound(dartId);

        Page<RoundResponse> responsePage = roundPage.map(round ->
            roundMapper.toDtoWithDart(
                round,
                dart,
                paidMap.getOrDefault(round.getId(), List.of())
            )
        );

        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getCurrentRoundByDartId(UUID dartId) {
        log.debug("Fetching current round for dart {}", dartId);

        Dart dart = findDartById(dartId);

        Page<Round> page = roundRepository.findCurrentRoundByDartId(
            dartId,
            RoundStatus.INPAYED,
            PageRequest.of(0, 1)
        );
        Round currentRound = page.hasContent()
            ? page.getContent().get(0)
            : null;
        if (currentRound == null) {
            log.warn("No current round found for dart {}", dartId);
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No current round found. All rounds may be completed."
            );
        }

        // Load paid member IDs for this specific round
        List<UUID> paidMemberIds = buildPaidMemberIdsForRound(
            currentRound.getId()
        );

        return roundMapper.toDtoWithDart(currentRound, dart, paidMemberIds);
    }

    @Override
    @Transactional
    public void deleteRound(UUID dartId, UUID roundId) {
        log.info("Deleting round {} for dart {}", roundId, dartId);
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round deletion not yet implemented"
        );
    }

    @Override
    @Transactional
    public RoundResponse markRoundAsPaid(UUID dartId, UUID roundId) {
        log.info("Marking round {} as paid for dart {}", roundId, dartId);
        Dart dart = findDartById(dartId);
        Round round = findRoundById(roundId);
        if (!round.getDart().getId().equals(dartId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Round does not belong to this dart"
            );
        }
        round.setStatus(RoundStatus.PAYED);
        round = roundRepository.save(round);

        // After marking paid, all members are effectively done — fetch updated list
        List<UUID> paidMemberIds = buildPaidMemberIdsForRound(round.getId());
        return roundMapper.toDtoWithDart(round, dart, paidMemberIds);
    }

    @Override
    @Transactional(readOnly = true)
    public RoundStatistics getRoundStatistics(UUID dartId) {
        log.debug("Fetching round statistics for dart {}", dartId);
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round statistics not yet implemented"
        );
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private Dart findDartById(UUID dartId) {
        return dartRepository
            .findById(dartId)
            .orElseThrow(() -> {
                log.warn("Dart not found with id: {}", dartId);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Dart not found with id: " + dartId
                );
            });
    }

    private Round findRoundById(UUID roundId) {
        return roundRepository
            .findById(roundId)
            .orElseThrow(() -> {
                log.warn("Round not found with id: {}", roundId);
                return new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Round not found with id: " + roundId
                );
            });
    }
}
