package com.tontin.platform.dto.dart.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AdminCreateDartRequest(
    @NotNull(message = "Organizer user id is required") UUID organizerUserId,
    @NotNull(message = "Dart payload is required") DartRequest dart
) {}
