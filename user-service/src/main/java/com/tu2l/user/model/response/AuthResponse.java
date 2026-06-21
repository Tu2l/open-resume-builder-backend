package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Schema(description = "Response returned on successful authentication or token refresh")
@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthResponse extends BaseResponse {

    @Schema(description = "Short-lived JWT access token used to authenticate subsequent API requests.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Refresh token used to obtain a new access token without re-authenticating.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String refreshToken;

    @Schema(description = "Token type — always `Bearer`.", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "UTC date-time at which the access token expires.", example = "2026-06-21T15:30:00")
    private LocalDateTime expiresIn;

    @Schema(description = "Public profile of the authenticated user.")
    private UserDTO user;
}
