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

    @Schema(hidden = true)
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

    @Schema(hidden = true)
    private String role;

    @Schema(hidden = true)
    private Boolean enabled;

    @Schema(hidden = true)
    private String createdAt;

    @Schema(hidden = true)
    private String updatedAt;
}
