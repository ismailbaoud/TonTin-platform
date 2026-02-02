package com.tontin.platform.service;

import java.util.UUID;


import com.tontin.platform.dto.Dart.request.DartRequest;
import com.tontin.platform.dto.Dart.response.DartResponse;

public interface DartService {
    DartResponse createDart(DartRequest request);
    DartResponse updateDart(DartRequest request, UUID id);
    DartResponse getDartDetails(UUID id);
    DartResponse deleteDart(UUID id);
}
