package com.tontin.platform.service.impl;

import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.round.OrderMethod;
import com.tontin.platform.service.RoundOrderService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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
     * Bidding model: Order by bid amount descending (highest bidder receives first).
     * When no bid data exists, uses a deterministic order: organizer first, then by member id
     * so that behaviour is reproducible until a real bidding feature (e.g. RoundBid entity) is added.
     *
     * @param members the list of members
     * @return ordered list (highest bid first; or organizer first then by member id)
     */
    private List<Member> determineBiddingOrder(List<Member> members) {
        log.debug("Using BIDDING_MODEL: organizer first, then by member id (bid data not yet available)");
        List<Member> ordered = new ArrayList<>(members);
        ordered.sort((m1, m2) -> {
            boolean m1IsOrg = m1.isOrganizer();
            boolean m2IsOrg = m2.isOrganizer();
            if (m1IsOrg && !m2IsOrg) return -1;
            if (!m1IsOrg && m2IsOrg) return 1;
            UUID id1 = m1.getId();
            UUID id2 = m2.getId();
            if (id1 == null && id2 == null) return 0;
            if (id1 == null) return 1;
            if (id2 == null) return -1;
            return id1.compareTo(id2);
        });
        log.debug("Bidding order determined: {} members", ordered.size());
        return ordered;
    }

    /**
     * Dynamique random: Full random order including organizer (no fixed first position).
     * Each time this is called, a new random permutation is produced. For round creation
     * this is used once; if round creation is later changed to assign per-round order
     * separately, this method can be called per round to get a new random order each time.
     *
     * @param members the list of members
     * @return shuffled list (all members, including organizer, in random order)
     */
    private List<Member> determineDynamiqueRandom(List<Member> members) {
        log.debug("Using DYNAMIQUE_RANDOM: full random order (all members shuffled)");
        List<Member> ordered = new ArrayList<>(members);
        Collections.shuffle(ordered, random);
        log.debug("Dynamique random order determined: {} members", ordered.size());
        return ordered;
    }
}
