package com.tontin.platform.dto.Dart.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tontin.platform.domain.enums.dart.DartStatus;

import lombok.Builder;

@Builder
public record DartResponse (
    String name,
    BigDecimal monthlyContribution,
    LocalDateTime startDate,
    String allocationMethod,
    DartStatus status
) {}