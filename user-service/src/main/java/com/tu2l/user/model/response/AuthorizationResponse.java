package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * Response for authorization/RBAC endpoints. Only the fields relevant to a given
 * endpoint are populated; the rest are {@code null} and omitted from JSON by the
 * service's {@code non_null} Jackson inclusion setting.
 */
@Schema(description = "Response for authorization (RBAC) endpoints. " +
                      "Only the fields relevant to the specific endpoint are populated; all others are omitted.")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthorizationResponse extends BaseResponse {

    @Schema(description = "Result of a permission check (`/check`). `true` if the caller has the requested permission.", example = "true", nullable = true)
    private Boolean allowed;

    @Schema(description = "A single role name — populated by `/roles/{userId}` (get/assign).", example = "USER", nullable = true)
    private String role;

    @Schema(description = "Full set of role names — populated by `GET /roles`.", example = "[\"ADMIN\",\"MODERATOR\",\"USER\",\"GUEST\"]", nullable = true)
    private Set<String> roles;

    @Schema(description = "Set of permission names — populated by `/permissions`, `/permissions/{userId}` and `/me/permissions`.",
            example = "[\"USER_READ\",\"USER_WRITE\",\"PDF_GENERATE\"]", nullable = true)
    private Set<String> permissions;
}
