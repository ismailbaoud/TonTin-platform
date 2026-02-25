package com.tontin.platform.service.impl;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.domain.RankHistory;
import com.tontin.platform.domain.User;
import com.tontin.platform.domain.enums.rank.PointAction;
import com.tontin.platform.domain.enums.rank.PointsType;
import com.tontin.platform.repository.DartRepository;
import com.tontin.platform.repository.RankHistoryRepository;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.PointsService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PointsServiceImpl implements PointsService {

    private final UserRepository userRepository;
    private final RankHistoryRepository rankHistoryRepository;
    private final DartRepository dartRepository;

    @Override
    @Transactional
    public Integer addPoints(UUID userId, PointAction action, UUID dartId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("Points not applied: user not found {}", userId);
            return null;
        }
        int delta = action.getPoints();
        int newTotal = (user.getPoints() != null ? user.getPoints() : 0) + delta;
        newTotal = Math.max(0, newTotal); // do not go below 0
        user.setPoints(newTotal);
        userRepository.save(user);

        Dart dart = dartId != null ? dartRepository.findById(dartId).orElse(null) : null;
        RankHistory history = RankHistory.builder()
            .points(delta)
            .type(action.toPointsType())
            .date(LocalDateTime.now())
            .action(action.getDescription())
            .user(user)
            .dart(dart)
            .build();
        rankHistoryRepository.save(history);
        log.debug("Points {} for user {} (action={}, newTotal={})", delta, userId, action, newTotal);
        return newTotal;
    }
}
