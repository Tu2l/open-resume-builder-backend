package com.tu2l.user.model.request;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Schema(description = "Request body for user authentication (login)")
@Data
public class LoginRequest implements BaseRequest {

    @Schema(description = "Registered email address", example = "john.doe@example.com")
    @NotBlank(message = CommonConstants.ValidationMessage.EMAIL_REQUIRED)
    @Email(message = CommonConstants.ValidationMessage.EMAIL_INVALID)
    private String email;

    @Schema(
            description = "Raw password encoded as a **Base64** string.",
            example = "UGFzc3dvcmQxMjMh"
    )
    @NotBlank(message = CommonConstants.ValidationMessage.PASSWORD_REQUIRED)
    @Pattern(
            regexp = CommonConstants.Pattern.BASE_64_PATTERN,
            message = "Password " + CommonConstants.ValidationMessage.BASE64_INVALID
    )
    private String password;

    @Schema(
            description = "When `true`, issues a longer-lived access token instead of the default short-lived one.",
            defaultValue = "false",
            example = "false"
    )
    private Boolean rememberMe = false;
}
