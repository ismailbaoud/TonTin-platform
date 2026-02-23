package com.tontin.platform.repository;

import com.tontin.platform.domain.DartMessageReaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

public interface DartMessageReactionRepository extends JpaRepository<DartMessageReaction, UUID> {

    List<DartMessageReaction> findByMessageId(UUID messageId);

    @Query("SELECT r FROM DartMessageReaction r JOIN FETCH r.member m JOIN FETCH m.user WHERE r.message.id = :messageId")
    List<DartMessageReaction> findByMessageIdWithMemberUser(@Param("messageId") UUID messageId);

    @Query("SELECT DISTINCT r FROM DartMessageReaction r JOIN FETCH r.member m JOIN FETCH m.user JOIN FETCH r.message WHERE r.message.id IN :messageIds")
    List<DartMessageReaction> findByMessageIdIn(@Param("messageIds") List<UUID> messageIds);

    Optional<DartMessageReaction> findByMessageIdAndMemberIdAndEmoji(
        UUID messageId,
        UUID memberId,
        String emoji
    );

    @Modifying
    @Query("DELETE FROM DartMessageReaction r WHERE r.message.id = :messageId AND r.member.id = :memberId AND r.emoji = :emoji")
    void deleteByMessageIdAndMemberIdAndEmoji(
        @Param("messageId") UUID messageId,
        @Param("memberId") UUID memberId,
        @Param("emoji") String emoji
    );
}
