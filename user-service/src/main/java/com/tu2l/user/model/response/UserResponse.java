package com.tu2l.user.model.response;

import com.tu2l.common.model.base.BaseResponse;
import com.tu2l.common.model.states.ResponseProcessingStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserResponse extends BaseResponse {
    private UserDTO user;

    public static UserResponse of(UserDTO user) {
        UserResponse r = new UserResponse();
        r.setUser(user);
        r.setStatus(ResponseProcessingStatus.SUCCESS);
        return r;
    }

    public static UserResponse of(UserDTO user, String message) {
        UserResponse r = of(user);
        r.setMessage(message);
        return r;
    }
}
