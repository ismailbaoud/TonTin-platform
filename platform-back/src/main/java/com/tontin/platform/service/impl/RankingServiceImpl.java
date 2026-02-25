package com.tontin.platform.service.impl;

import com.tontin.platform.config.SecurityUtils;
import com.tontin.platform.domain.User;
import com.tontin.platform.dto.dart.response.PageResponse;
import com.tontin.platform.dto.rank.response.RankingEntryResponse;
import com.tontin.platform.repository.UserRepository;
import com.tontin.platform.service.RankingService;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {

    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RankingEntryResponse> getRankings(int page, int size) {
        Sort order = Sort.by(Sort.Order.desc("points"), Sort.Order.asc("userName"));
        Page<User> userPage = userRepository.findAllByOrderByPointsDesc(
            PageRequest.of(page, size, order)
        );
        int startRank = page * size + 1;
        List<RankingEntryResponse> withRank = new ArrayList<>();
        for (User u : userPage.getContent()) {
            withRank.add(toEntry(u, startRank++));
        }
        return PageResponse.<RankingEntryResponse>builder()
            .content(withRank)
            .page(page)
            .size(size)
            .totalElements(userPage.getTotalElements())
            .totalPages(userPage.getTotalPages())
            .first(userPage.isFirst())
            .last(userPage.isLast())
            .hasNext(userPage.hasNext())
            .hasPrevious(userPage.hasPrevious())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RankingEntryResponse getCurrentUserRanking() {
        UUID userId = securityUtils.requireCurrentUserId();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return null;
        int maxSize = 100_000;
        Sort order = Sort.by(Sort.Order.desc("points"), Sort.Order.asc("userName"));
        List<User> allOrdered = userRepository.findAllByOrderByPointsDesc(
            PageRequest.of(0, maxSize, order)
        ).getContent();
        int rank = 1;
        for (User u : allOrdered) {
            if (u.getId().equals(userId)) {
                return toEntry(user, rank);
            }
            rank++;
        }
        return toEntry(user, rank);
    }

    private static RankingEntryResponse toEntry(User u, int rank) {
        int pts = (u.getPoints() == null) ? 0 : u.getPoints().intValue();
        String pictureB64 = u.getPicture() != null && u.getPicture().length > 0
            ? Base64.getEncoder().encodeToString(u.getPicture())
            : null;
        return RankingEntryResponse.builder()
            .id(u.getId())
            .userName(u.getUserName() != null ? u.getUserName() : "")
            .points(pts)
            .rank(rank)
            .picture(pictureB64)
            .build();
    }
}
