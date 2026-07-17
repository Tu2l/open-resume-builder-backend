package com.tu2l.user.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Public profile of a user returned inside authentication responses")
@Data
@JsonIgnoreProperties(
        ignoreUnknown = true,
        value = {"id", "role", "enabled", "createdAt", "updatedAt"}
)
public class UserDTO {

    @Schema(description = "Internal user identifier.", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Unique username.", example = "john.doe")
    private String username;

    @Schema(description = "User's email address.", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name.", example = "John")
    private String firstName;

    @Schema(description = "User's middle name.", example = "Michael", nullable = true)
    private String middleName;

    @Schema(description = "User's last name.", example = "Doe")
    private String lastName;

    @Schema(description = "User's phone number.", example = "+1-555-000-1234", nullable = true)
    private String phoneNumber;

    @Schema(description = "Assigned role.", example = "USER", accessMode = Schema.AccessMode.READ_ONLY)
    private String role;

    @Schema(description = "Whether the account is active.", example = "true", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean enabled;

    @Schema(description = "ISO-8601 timestamp of account creation.", example = "2026-01-15T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String createdAt;

    @Schema(description = "ISO-8601 timestamp of the last account update.", example = "2026-06-21T08:00:00", accessMode = Schema.AccessMode.READ_ONLY)
    private String updatedAt;
}
