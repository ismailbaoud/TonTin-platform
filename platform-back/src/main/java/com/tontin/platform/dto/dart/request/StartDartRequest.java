package com.tontin.platform.dto.dart.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to start a dart. When startAnyway is true, pending members are set to LEAVED and excluded from the dart.")
public record StartDartRequest(
    @Schema(
        description = "If true, start the dart even with pending members; pending members will be removed (status set to LEAVED) and not included in rounds.",
        example = "false"
    )
    boolean startAnyway
) {
    /** Default: do not start if there are pending members. */
    public static StartDartRequest defaultRequest() {
        return new StartDartRequest(false);
    }
}
