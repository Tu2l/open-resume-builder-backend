package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Schema(description = "Request body for re-sending an email-verification link")
@Data
public class ResendVerificationRequest implements BaseRequest {

    @Schema(
            description = "Email address associated with the account. " +
                          "A verification link is sent if the account exists and is not yet verified.",
            example = "john.doe@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
}
