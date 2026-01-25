package com.tontin.platform.service;

import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;
import java.util.UUID;

public interface DartService {
    DartResponse createDart(DartRequest request);
    DartResponse updateDart(DartRequest request, UUID id);
    DartResponse getDartDetails(UUID id);
    DartResponse deleteDart(UUID id);
}
