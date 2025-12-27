package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest implements BaseRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
    @NotBlank(message = "Username is required")
    private String username;
}
