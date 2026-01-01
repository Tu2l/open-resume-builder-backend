package com.tu2l.user.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(
        ignoreUnknown = true,
        value = {"id", "role", "enabled", "createdAt", "updatedAt"}
)
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String role;
    private Boolean enabled;
    private String createdAt;
    private String updatedAt;
}
