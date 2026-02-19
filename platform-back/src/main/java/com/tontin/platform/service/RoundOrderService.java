package com.tontin.platform.service;

import com.tontin.platform.domain.Member;
import com.tontin.platform.domain.enums.round.OrderMethod;
import java.util.List;

/**
 * Service for determining the order of members in rounds based on allocation method.
 */
public interface RoundOrderService {

    /**
     * Determine the order of members for rounds based on the allocation method.
     *
     * @param members the list of active members
     * @param orderMethod the order method to use
     * @return ordered list of members (the order they will receive money)
     */
    List<Member> determineMemberOrder(List<Member> members, OrderMethod orderMethod);
}
