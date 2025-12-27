package com.tu2l.user.model.request;

import com.tu2l.common.model.base.BaseRequest;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest implements BaseRequest {

    @Email(message = "Email must be valid")
    private String email; // email cant be updated

    private String firstName;
    private String lastName;
    private String phoneNumber;
}
