package com.tontin.platform.mapper;

import org.mapstruct.Mapper;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.dto.dart.request.DartRequest;
import com.tontin.platform.dto.dart.response.DartResponse;

@Mapper(componentModel = "spring")
public interface DartMapper {
    Dart toEntity(DartRequest request);
    DartResponse toDto(Dart dart);
}
