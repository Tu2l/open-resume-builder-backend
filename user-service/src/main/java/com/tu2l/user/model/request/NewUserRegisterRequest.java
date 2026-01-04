package com.tu2l.user.model.request;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseRequest;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NewUserRegisterRequest implements BaseRequest {

    @NotBlank(message = CommonConstants.ValidationMessage.USERNAME_REQUIRED)
    @Size(
            min = CommonConstants.Validation.USERNAME_MIN_LENGTH,
            max = CommonConstants.Validation.USERNAME_MAX_LENGTH,
            message = CommonConstants.ValidationMessage.USERNAME_SIZE
    )
    @Pattern(
            regexp = CommonConstants.Pattern.USERNAME_PATTERN,
            message = CommonConstants.ValidationMessage.USERNAME_PATTERN_MSG
    )
    private String username;

    @NotBlank(message = CommonConstants.ValidationMessage.EMAIL_REQUIRED)
    @Email(message = CommonConstants.ValidationMessage.EMAIL_INVALID)
    private String email;

    @NotBlank(message = CommonConstants.ValidationMessage.PASSWORD_REQUIRED)
    @Size(
            min = CommonConstants.Validation.PASSWORD_MIN_LENGTH,
            max = CommonConstants.Validation.PASSWORD_MAX_LENGTH,
            message = CommonConstants.ValidationMessage.PASSWORD_SIZE
    )
    @Pattern(
            regexp = CommonConstants.Pattern.PASSWORD_PATTERN,
            message = CommonConstants.ValidationMessage.PASSWORD_PATTERN_MSG
    )
    private String password;
}
