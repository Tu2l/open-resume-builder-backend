package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest implements BaseRequest {
    
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    private String password;
    
    private Boolean rememberMe = false;
}
