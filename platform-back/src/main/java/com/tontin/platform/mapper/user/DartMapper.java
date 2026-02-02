package com.tontin.platform.mapper.user;

import org.mapstruct.Mapper;

import com.tontin.platform.domain.Dart;
import com.tontin.platform.dto.Dart.request.DartRequest;
import com.tontin.platform.dto.Dart.response.DartResponse;

@Mapper(componentModel = "spring")
public interface DartMapper {
    Dart toEntity(DartRequest request);
    DartResponse toDto(Dart dart);
}
