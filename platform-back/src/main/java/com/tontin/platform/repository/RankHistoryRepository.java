package com.tontin.platform.repository;

import com.tontin.platform.domain.RankHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RankHistoryRepository extends JpaRepository<RankHistory, UUID> {

    List<RankHistory> findAllByUserIdOrderByDateDesc(UUID userId, Pageable pageable);

    @Query("SELECT r FROM RankHistory r WHERE r.user.id = :userId ORDER BY r.date DESC")
    List<RankHistory> findAllByUserId(@Param("userId") UUID userId);

    List<RankHistory> findAllByUserIdAndDartIdOrderByDateDesc(UUID userId, UUID dartId, Pageable pageable);
}
