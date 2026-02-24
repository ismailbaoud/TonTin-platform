package com.tontin.platform.repository;

import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.user.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    public Optional<User> findByEmail(String email);

    Optional<User> findByUserNameIgnoreCase(String userName);

    public Optional<User> findByVerificationCode(String code);

    /**
     * Search users by username (case-insensitive, partial match)
     *
     * @param username the username to search for
     * @return list of matching users
     */
    @Query("""
            SELECT u
            FROM User u
            WHERE LOWER(u.userName) LIKE LOWER(CONCAT('%', :username, '%'))
            AND u.status = :status
            """)
    List<User> findByUserNameAndStatus(
            @Param("username") String username,
            @Param("status") UserStatus status);

    /**
     * Find users ordered by points descending (for leaderboard). Only active users.
     */
    Page<User> findByStatusOrderByPointsDesc(UserStatus status, Pageable pageable);

    /**
     * Find all users ordered by points descending (for leaderboard). Includes all statuses.
     */
    Page<User> findAllByOrderByPointsDesc(Pageable pageable);
}
