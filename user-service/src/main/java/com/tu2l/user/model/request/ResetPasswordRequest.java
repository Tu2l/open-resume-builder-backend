package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Request body for completing a password reset")
@Data
public class ResetPasswordRequest implements BaseRequest {

    @Schema(
            description = "Single-use reset token received via email.",
            example = "eyJhbGciOiJIUzI1NiJ9..."
    )
    @NotBlank(message = "Reset token is required")
    private String resetToken;

    @Schema(
            description = "The new password in **plain text** (not Base64). " +
                          "Must be 8–100 characters and include at least one uppercase letter, " +
                          "one lowercase letter, one digit and one special character (@$!%*?&).",
            example = "NewP@ssword1",
            minLength = 8,
            maxLength = 100
    )
    @NotBlank(message = "New password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$",
             message = "Password must contain at least one uppercase letter, one lowercase letter, one number and one special character")
    private String newPassword;
}
