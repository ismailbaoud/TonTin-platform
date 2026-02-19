package com.tontin.platform.repository;

import com.tontin.platform.domain.Round;
import com.tontin.platform.domain.enums.round.RoundStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository interface for Round entity operations.
 */
public interface RoundRepository extends JpaRepository<Round, UUID> {

    /**
     * Find all rounds for a specific dart.
     *
     * @param dartId the dart ID
     * @return list of rounds ordered by round number
     */
    @Query("SELECT r FROM Round r WHERE r.dart.id = :dartId ORDER BY r.number ASC")
    List<Round> findAllByDartId(@Param("dartId") UUID dartId);

    /**
     * Find all rounds for a specific dart with pagination.
     *
     * @param dartId the dart ID
     * @param pageable pagination information
     * @return page of rounds ordered by round number
     */
    @Query("SELECT r FROM Round r WHERE r.dart.id = :dartId ORDER BY r.number ASC")
    Page<Round> findAllByDartId(@Param("dartId") UUID dartId, Pageable pageable);

    /**
     * Find rounds for a specific dart filtered by status.
     *
     * @param dartId the dart ID
     * @param status the round status
     * @return list of rounds ordered by round number
     */
    @Query("SELECT r FROM Round r WHERE r.dart.id = :dartId AND r.status = :status ORDER BY r.number ASC")
    List<Round> findAllByDartIdAndStatus(
        @Param("dartId") UUID dartId,
        @Param("status") RoundStatus status
    );

    /**
     * Find a specific round by dart ID and round number.
     *
     * @param dartId the dart ID
     * @param number the round number
     * @return optional round
     */
    @Query("SELECT r FROM Round r WHERE r.dart.id = :dartId AND r.number = :number")
    Optional<Round> findByDartIdAndNumber(
        @Param("dartId") UUID dartId,
        @Param("number") Integer number
    );

    /**
     * Find the current (next unpaid) round for a dart, with recipient and user loaded.
     *
     * @param dartId the dart ID
     * @param status the round status (e.g. INPAYED)
     * @param pageable limit to 1 result
     * @return page with one round
     */
    @Query("SELECT DISTINCT r FROM Round r LEFT JOIN FETCH r.recipient rec LEFT JOIN FETCH rec.user WHERE r.dart.id = :dartId AND r.status = :status ORDER BY r.number ASC")
    org.springframework.data.domain.Page<Round> findCurrentRoundByDartId(
        @Param("dartId") UUID dartId,
        @Param("status") RoundStatus status,
        org.springframework.data.domain.Pageable pageable
    );

    /**
     * Count rounds for a specific dart.
     *
     * @param dartId the dart ID
     * @return count of rounds
     */
    @Query("SELECT COUNT(r) FROM Round r WHERE r.dart.id = :dartId")
    long countByDartId(@Param("dartId") UUID dartId);

    /**
     * Count paid rounds for a specific dart.
     *
     * @param dartId the dart ID
     * @return count of paid rounds
     */
    @Query("SELECT COUNT(r) FROM Round r WHERE r.dart.id = :dartId AND r.status = com.tontin.platform.domain.enums.round.RoundStatus.PAYED")
    long countPaidRoundsByDartId(@Param("dartId") UUID dartId);

    /**
     * Check if a round exists for a dart with a specific number.
     *
     * @param dartId the dart ID
     * @param number the round number
     * @return true if round exists
     */
    @Query("SELECT COUNT(r) > 0 FROM Round r WHERE r.dart.id = :dartId AND r.number = :number")
    boolean existsByDartIdAndNumber(
        @Param("dartId") UUID dartId,
        @Param("number") Integer number
    );
}
