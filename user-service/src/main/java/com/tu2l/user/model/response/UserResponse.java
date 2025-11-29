package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends BaseResponse {
    private UserDTO user;
}
