package com.tu2l.user.model.request;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseRequest;
import com.tu2l.user.validation.ValidEncodedPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordRequest implements BaseRequest {

    @NotBlank(message = "Current password is required")
    @Pattern(
            regexp = CommonConstants.Pattern.BASE_64_PATTERN,
            message = "Current password " + CommonConstants.ValidationMessage.BASE64_INVALID
    )
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @Pattern(
            regexp = CommonConstants.Pattern.BASE_64_PATTERN,
            message = "New password " + CommonConstants.ValidationMessage.BASE64_INVALID
    )
    @ValidEncodedPassword
    private String newPassword;
}
