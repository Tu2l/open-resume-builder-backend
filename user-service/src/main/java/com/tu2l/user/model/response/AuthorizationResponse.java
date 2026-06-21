package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

/**
 * Response for authorization/RBAC endpoints. Only the fields relevant to a given
 * endpoint are populated; the rest are {@code null} and omitted from JSON by the
 * service's {@code non_null} Jackson inclusion setting.
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthorizationResponse extends BaseResponse {
    /** Result of a permission check ({@code /check}). */
    private Boolean allowed;
    /** A single user's role ({@code /roles/{userId}}, after assignment). */
    private String role;
    /** All available roles ({@code /roles}). */
    private Set<String> roles;
    /** Permissions for a role/user ({@code /permissions}, {@code /me/permissions}). */
    private Set<String> permissions;
}
