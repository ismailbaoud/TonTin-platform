package com.tontin.platform.repository;

import com.tontin.platform.domain.DartMessage;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DartMessageRepository extends JpaRepository<DartMessage, UUID> {

    @EntityGraph(attributePaths = { "sender", "sender.user", "dart" })
    Page<DartMessage> findByDartIdOrderByCreatedAtAsc(UUID dartId, Pageable pageable);

    @Query("SELECT m FROM DartMessage m LEFT JOIN FETCH m.sender s LEFT JOIN FETCH s.user LEFT JOIN FETCH m.dart WHERE m.id = :id")
    java.util.Optional<DartMessage> findByIdWithSenderAndUser(@Param("id") UUID id);

    @Modifying
    @Query("DELETE FROM DartMessage m WHERE m.dart.id = :dartId")
    void deleteAllByDartId(@Param("dartId") UUID dartId);
}
