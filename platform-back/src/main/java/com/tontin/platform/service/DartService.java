package com.tontin.platform.service;

import com.tontin.platform.domain.enums.dart.DartStatus;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import com.tontin.platform.dto.dart.response.PageResponse;
import java.util.UUID;

public interface DartService {
    DartResponse createDart(DartRequest request);
    DartResponse updateDart(DartRequest request, UUID id);
    DartResponse getDartDetails(UUID id);
    DartResponse deleteDart(UUID id);

    /**
     * Start a dart (organizer only)
     * Sets the start date and changes status to ACTIVE if minimum members reached
     *
     * @param id the dart id
     * @return the updated dart
     */
    DartResponse startDart(UUID id);

    /**
     * Get all darts for the current authenticated user
     *
     * @param status optional status filter
     * @param page page number (0-based)
     * @param size page size
     * @return paginated list of darts
     */
    PageResponse<DartResponse> getMyDarts(
        DartStatus status,
        int page,
        int size
    );
}
