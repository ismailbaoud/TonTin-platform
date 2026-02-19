package com.tontin.platform.service.impl;

import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.round.OrderMethod;
import com.tontin.platform.service.RoundOrderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Implementation of RoundOrderService for determining member order.
 */
@Service
@Slf4j
public class RoundOrderServiceImpl implements RoundOrderService {

    private final Random random = new Random();

    @Override
    public List<Member> determineMemberOrder(List<Member> members, OrderMethod orderMethod) {
        if (members == null || members.isEmpty()) {
            log.warn("Empty member list provided for order determination");
            return new ArrayList<>();
        }

        log.info("Determining member order using method: {} for {} members", orderMethod, members.size());

        return switch (orderMethod) {
            case FIXED_ORDER -> determineFixedOrder(members);
            case RANDOM_ONCE -> determineRandomOnce(members);
            case BIDDING_MODEL -> determineBiddingOrder(members);
            case DYNAMIQUE_RANDOM -> determineDynamiqueRandom(members);
        };
    }

    /**
     * Fixed order: Members receive money in the order they joined (by joinedAt date).
     * Organizer is always first.
     *
     * @param members the list of members
     * @return ordered list
     */
    private List<Member> determineFixedOrder(List<Member> members) {
        log.debug("Using FIXED_ORDER: sorting by join date, organizer first");
        List<Member> ordered = new ArrayList<>(members);

        // Sort: organizer first, then by join date
        ordered.sort((m1, m2) -> {
            // Organizer always comes first
            boolean m1IsOrg = m1.isOrganizer();
            boolean m2IsOrg = m2.isOrganizer();
            if (m1IsOrg && !m2IsOrg) {
                return -1;
            }
            if (!m1IsOrg && m2IsOrg) {
                return 1;
            }
            // Both same type, sort by join date
            return m1.getJoinedAt().compareTo(m2.getJoinedAt());
        });

        log.debug("Fixed order determined: {} members", ordered.size());
        return ordered;
    }

    /**
     * Random once: Random order determined once at the start.
     * Organizer is still first, then randomize others.
     *
     * @param members the list of members
     * @return ordered list
     */
    private List<Member> determineRandomOnce(List<Member> members) {
        log.debug("Using RANDOM_ONCE: organizer first, then random order");
        List<Member> organizer = new ArrayList<>();
        List<Member> regularMembers = new ArrayList<>();

        // Separate organizer from regular members
        for (Member member : members) {
            if (member.isOrganizer()) {
                organizer.add(member);
            } else {
                regularMembers.add(member);
            }
        }

        // Shuffle regular members
        Collections.shuffle(regularMembers, random);

        // Combine: organizer first, then shuffled regular members
        List<Member> ordered = new ArrayList<>();
        ordered.addAll(organizer);
        ordered.addAll(regularMembers);

        log.debug("Random once order determined: {} members", ordered.size());
        return ordered;
    }

    /**
     * Bidding model: Order based on bidding (highest bidder first).
     * For now, falls back to fixed order until bidding is implemented.
     *
     * @param members the list of members
     * @return ordered list
     */
    private List<Member> determineBiddingOrder(List<Member> members) {
        log.debug("Using BIDDING_MODEL: falling back to fixed order (bidding not yet implemented)");
        // TODO: Implement bidding logic when bidding feature is added
        // For now, use fixed order
        return determineFixedOrder(members);
    }

    /**
     * Dynamique random: Random order for each round.
     * For initial round creation, we'll use random once, but mark it as dynamic.
     *
     * @param members the list of members
     * @return ordered list
     */
    private List<Member> determineDynamiqueRandom(List<Member> members) {
        log.debug("Using DYNAMIQUE_RANDOM: random order (will be randomized each round)");
        // For initial creation, use random once
        // The actual randomization per round will be handled when marking rounds as paid
        return determineRandomOnce(members);
    }
}
