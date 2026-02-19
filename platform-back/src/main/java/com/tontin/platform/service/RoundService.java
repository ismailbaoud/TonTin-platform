package com.tontin.platform.service;

import com.tontin.platform.dto.round.request.CreateRoundsRequest;
import com.tontin.platform.dto.round.request.RoundRequest;
import com.tontin.platform.dto.round.response.RoundResponse;
import com.tontin.platform.dto.dart.response.PageResponse;
import java.util.List;
import java.util.UUID;

/**
 * Service interface for Round management operations.
 */
public interface RoundService {

    /**
     * Create rounds for a dart when it starts.
     * Rounds are created based on the dart's order method and member count.
     *
     * @param request the create rounds request containing dart ID
     * @return list of created round responses
     */
    List<RoundResponse> createRoundsForDart(CreateRoundsRequest request);

    /**
     * Create a single round.
     *
     * @param dartId the dart ID
     * @param request the round creation request
     * @return the created round response
     */
    RoundResponse createRound(UUID dartId, RoundRequest request);

    /**
     * Update an existing round.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @param request the round update request
     * @return the updated round response
     */
    RoundResponse updateRound(UUID dartId, UUID roundId, RoundRequest request);

    /**
     * Get a round by ID.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @return the round response
     */
    RoundResponse getRoundById(UUID dartId, UUID roundId);

    /**
     * Get all rounds for a dart.
     *
     * @param dartId the dart ID
     * @return list of round responses
     */
    List<RoundResponse> getAllRoundsByDartId(UUID dartId);

    /**
     * Get all rounds for a dart with pagination.
     *
     * @param dartId the dart ID
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of round responses
     */
    PageResponse<RoundResponse> getAllRoundsByDartId(UUID dartId, int page, int size);

    /**
     * Get the current (next unpaid) round for a dart.
     *
     * @param dartId the dart ID
     * @return the current round response
     * @throws ResponseStatusException if no current round found (all rounds may be paid or no rounds exist)
     */
    RoundResponse getCurrentRoundByDartId(UUID dartId);

    /**
     * Delete a round.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     */
    void deleteRound(UUID dartId, UUID roundId);

    /**
     * Mark a round as paid.
     *
     * @param dartId the dart ID
     * @param roundId the round ID
     * @return the updated round response
     */
    RoundResponse markRoundAsPaid(UUID dartId, UUID roundId);

    /**
     * Get round statistics for a dart.
     *
     * @param dartId the dart ID
     * @return round statistics
     */
    RoundStatistics getRoundStatistics(UUID dartId);

    /**
     * Round statistics record.
     *
     * @param totalRounds total number of rounds
     * @param paidRounds number of paid rounds
     * @param unpaidRounds number of unpaid rounds
     * @param currentRoundNumber current round number (null if all paid)
     */
    record RoundStatistics(
        long totalRounds,
        long paidRounds,
        long unpaidRounds,
        Integer currentRoundNumber
    ) {}
}
