package com.tu2l.user.model.response;

import lombok.Data;

@Data
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
