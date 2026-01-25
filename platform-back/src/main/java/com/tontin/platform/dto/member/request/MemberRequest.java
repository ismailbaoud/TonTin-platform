package com.tontin.platform.dto.member.request;

import com.tontin.platform.domain.enums.dart.DartPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for adding or updating a member in a dart.
 *
 * <p>This immutable record encapsulates the permission level that should be
 * assigned to a member when they join or when their role is updated.</p>
 *
 * @param permission The permission level for the member (ORGANIZER or MEMBER)
 */
@Schema(
    description = "Request object for adding or updating a member in a dart"
)
public record MemberRequest(
    @NotNull(message = "Permission is required")
    @Schema(
        description = "Permission level for the member",
        example = "MEMBER",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = { "ORGANIZER", "MEMBER" }
    )
    DartPermission permission
) {
    /**
     * Checks if the permission level is ORGANIZER.
     *
     * @return true if the permission is ORGANIZER
     */
    public boolean isOrganizer() {
        return permission == DartPermission.ORGANIZER;
    }

    /**
     * Checks if the permission level is MEMBER.
     *
     * @return true if the permission is MEMBER
     */
    public boolean isMember() {
        return permission == DartPermission.MEMBER;
    }
}
