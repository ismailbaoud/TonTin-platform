package com.tontin.platform.repository;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.enums.dart.DartStatus;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DartRepository extends JpaRepository<Dart, UUID> {
    /**
     * Find all darts where the user is a member
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of darts
     */
    @Query(
        "SELECT DISTINCT d FROM Dart d JOIN d.members m WHERE m.user.id = :userId ORDER BY d.createdAt DESC"
    )
    Page<Dart> findAllByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find all darts where the user is a member and dart has specific status
     *
     * @param userId the user ID
     * @param status the dart status
     * @param pageable pagination information
     * @return page of darts
     */
    @Query(
        "SELECT DISTINCT d FROM Dart d JOIN d.members m WHERE m.user.id = :userId AND d.status = :status ORDER BY d.createdAt DESC"
    )
    Page<Dart> findAllByUserIdAndStatus(
        @Param("userId") UUID userId,
        @Param("status") DartStatus status,
        Pageable pageable
    );
}
