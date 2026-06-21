package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request body for refreshing an access token")
@Data
public class RefreshTokenRequest implements BaseRequest {

    @Schema(description = "The refresh token issued during login or a previous refresh.", example = "eyJhbGciOiJIUzI1NiJ9...")
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    @Schema(description = "Username of the token owner — must match the subject embedded in the refresh token.", example = "john.doe")
    @NotBlank(message = "Username is required")
    private String username;
}
