package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.Round;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.round.RoundStatus;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.round.request.CreateRoundsRequest;
import com.tontin.platform.dto.round.request.RoundRequest;
import com.tontin.platform.dto.round.response.RoundResponse;
import com.tontin.platform.mapper.RoundMapper;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.RoundRepository;
import com.tontin.platform.service.RoundOrderService;
import com.tontin.platform.service.RoundService;
import com.tontin.platform.util.PaymentFrequencyUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

/**
 * Service implementation for Round management operations.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RoundServiceImpl implements RoundService {

    private final RoundRepository roundRepository;
    private final DartRepository dartRepository;
    private final RoundMapper roundMapper;
    private final SecurityUtils securityUtils;
    private final RoundOrderService roundOrderService;

    @Override
    @Transactional
    public List<RoundResponse> createRoundsForDart(CreateRoundsRequest request) {
        log.info("Creating rounds for dart with ID: {}", request.dartId());

        // Find the dart
        Dart dart = findDartById(request.dartId());

        // Verify dart is started
        if (dart.getStartDate() == null) {
            log.warn("Cannot create rounds for dart {} - dart not started yet", request.dartId());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot create rounds: dart must be started first"
            );
        }

        // Check if rounds already exist
        long existingRounds = roundRepository.countByDartId(request.dartId());
        if (existingRounds > 0) {
            log.warn("Rounds already exist for dart {} - found {} rounds", request.dartId(), existingRounds);
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Rounds already exist for this dart. Cannot create duplicates."
            );
        }

        // Get active members
        List<Member> activeMembers = dart.getActiveMembers();
        if (activeMembers.size() < 2) {
            log.warn("Cannot create rounds for dart {} - insufficient members: {}", request.dartId(), activeMembers.size());
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Cannot create rounds: minimum 2 active members required"
            );
        }

        // Determine member order based on allocation method
        List<Member> orderedMembers = roundOrderService.determineMemberOrder(
            activeMembers,
            dart.getOrderMethod()
        );

        log.info("Determined member order for dart {}: {} members", request.dartId(), orderedMembers.size());
        orderedMembers.forEach(m -> log.debug("  - Member {}: {}", m.getId(), m.getUser().getUserName()));

        // Calculate round amount (total contribution = monthlyContribution * memberCount)
        double roundAmount = dart.getMonthlyContribution()
            .multiply(BigDecimal.valueOf(activeMembers.size()))
            .doubleValue();

        // Create rounds
        List<Round> rounds = new ArrayList<>();
        LocalDateTime startDate = dart.getStartDate();

        for (int i = 0; i < orderedMembers.size(); i++) {
            Member recipient = orderedMembers.get(i);
            int roundNumber = i + 1;

            // Calculate round date based on payment frequency
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
            log.debug("Created round {} for dart {} - recipient: {}, date: {}", 
                roundNumber, request.dartId(), recipient.getUser().getUserName(), roundDate);
        }

        // Save all rounds
        List<Round> savedRounds = roundRepository.saveAll(rounds);
        log.info("Successfully created {} rounds for dart {}", savedRounds.size(), request.dartId());

        // Convert to DTOs
        List<RoundResponse> responses = savedRounds.stream()
            .map(round -> roundMapper.toDtoWithDart(round, dart))
            .toList();

        return responses;
    }

    @Override
    @Transactional
    public RoundResponse createRound(UUID dartId, RoundRequest request) {
        log.info("Creating round {} for dart {}", request.number(), dartId);
        // TODO: Implement round creation logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round creation not yet implemented"
        );
    }

    @Override
    @Transactional
    public RoundResponse updateRound(UUID dartId, UUID roundId, RoundRequest request) {
        log.info("Updating round {} for dart {}", roundId, dartId);
        // TODO: Implement round update logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round update not yet implemented"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getRoundById(UUID dartId, UUID roundId) {
        log.debug("Fetching round {} for dart {}", roundId, dartId);
        // TODO: Implement round retrieval logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round retrieval not yet implemented"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoundResponse> getAllRoundsByDartId(UUID dartId) {
        log.debug("Fetching all rounds for dart {}", dartId);
        
        // Verify dart exists
        Dart dart = findDartById(dartId);
        
        // Get all rounds for this dart
        List<Round> rounds = roundRepository.findAllByDartId(dartId);
        log.info("Found {} rounds for dart {}", rounds.size(), dartId);
        
        // Convert to DTOs
        return rounds.stream()
            .map(round -> roundMapper.toDtoWithDart(round, dart))
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoundResponse> getAllRoundsByDartId(UUID dartId, int page, int size) {
        log.info("Fetching rounds for dart {} with pagination - page: {}, size: {}", dartId, page, size);
        
        // Verify dart exists
        Dart dart = findDartById(dartId);
        
        // Get paginated rounds
        Pageable pageable = PageRequest.of(page, size);
        Page<Round> roundPage = roundRepository.findAllByDartId(dartId, pageable);
        
        // Convert to DTOs
        Page<RoundResponse> responsePage = roundPage.map(round -> roundMapper.toDtoWithDart(round, dart));
        
        return PageResponse.of(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public RoundResponse getCurrentRoundByDartId(UUID dartId) {
        log.debug("Fetching current round for dart {}", dartId);
        
        // Verify dart exists
        Dart dart = findDartById(dartId);
        
        // Get current (next unpaid) round (first INPAYED round by number)
        Page<Round> page = roundRepository.findCurrentRoundByDartId(
            dartId,
            RoundStatus.INPAYED,
            PageRequest.of(0, 1)
        );
        Round currentRound = page.hasContent() ? page.getContent().get(0) : null;
        if (currentRound == null) {
            log.warn("No current round found for dart {}", dartId);
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No current round found. All rounds may be completed."
            );
        }
        return roundMapper.toDtoWithDart(currentRound, dart);
    }

    @Override
    @Transactional
    public void deleteRound(UUID dartId, UUID roundId) {
        log.info("Deleting round {} for dart {}", roundId, dartId);
        // TODO: Implement round deletion logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round deletion not yet implemented"
        );
    }

    @Override
    @Transactional
    public RoundResponse markRoundAsPaid(UUID dartId, UUID roundId) {
        log.info("Marking round {} as paid for dart {}", roundId, dartId);
        // TODO: Implement mark round as paid logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Mark round as paid not yet implemented"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public RoundStatistics getRoundStatistics(UUID dartId) {
        log.debug("Fetching round statistics for dart {}", dartId);
        // TODO: Implement round statistics logic
        throw new ResponseStatusException(
            HttpStatus.NOT_IMPLEMENTED,
            "Round statistics not yet implemented"
        );
    }

    /**
     * Helper method to find a dart by ID.
     *
     * @param dartId the dart ID
     * @return the dart entity
     * @throws ResponseStatusException if dart not found
     */
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

    /**
     * Helper method to find a round by ID.
     *
     * @param roundId the round ID
     * @return the round entity
     * @throws ResponseStatusException if round not found
     */
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
