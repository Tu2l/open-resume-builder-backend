package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Builder
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthResponse extends BaseResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private LocalDateTime expiresIn;
    private UserDTO user;
}
