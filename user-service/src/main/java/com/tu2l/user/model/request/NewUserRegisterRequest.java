package com.tu2l.user.model.request;

import com.tu2l.common.constant.CommonConstants;
import com.tu2l.common.model.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request body for registering a new user account")
@Data
public class NewUserRegisterRequest implements BaseRequest {

    @Schema(
            description = "Unique username. Allowed characters: letters, digits, dots, underscores and hyphens.",
            example = "john.doe",
            minLength = 3,
            maxLength = 50
    )
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

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @NotBlank(message = CommonConstants.ValidationMessage.EMAIL_REQUIRED)
    @Email(message = CommonConstants.ValidationMessage.EMAIL_INVALID)
    private String email;

    @Schema(
            description = "Raw password encoded as a **Base64** string. " +
                          "The decoded value must be 8–100 characters and contain at least one uppercase letter, " +
                          "one lowercase letter, one digit and one special character (@$!%*?&).",
            example = "UGFzc3dvcmQxMjMh"
    )
    @NotBlank(message = CommonConstants.ValidationMessage.PASSWORD_REQUIRED)
    @Pattern(
            regexp = CommonConstants.Pattern.BASE_64_PATTERN,
            message = "Password " + CommonConstants.ValidationMessage.BASE64_INVALID
    )
    private String password;
}
