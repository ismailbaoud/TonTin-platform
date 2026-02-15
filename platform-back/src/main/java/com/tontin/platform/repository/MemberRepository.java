package com.tontin.platform.repository;

import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.dart.DartPermission;
import com.tontin.platform.domain.enums.member.MemberStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByIdAndDartId(UUID memberId, UUID dartId);

    Optional<Member> findByDartIdAndUserId(UUID dartId, UUID userId);

    boolean existsByUserIdAndDartId(UUID userId, UUID dartId);

    boolean existsByDartIdAndUserIdAndPermission(
        UUID dartId,
        UUID userId,
        DartPermission permission
    );

    long countByDartIdAndPermission(UUID dartId, DartPermission permission);

    long countByDartIdAndStatus(UUID dartId, MemberStatus status);

    List<Member> findAllByDartId(UUID dartId);
}
